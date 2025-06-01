    private boolean doProcessSequential(final Exchange original, final AtomicExchange result,
                                        final Iterable<ProcessorExchangePair> pairs, final Iterator<ProcessorExchangePair> it,
                                        final ProcessorExchangePair pair, final AsyncCallback callback, final AtomicInteger total) {
        boolean sync = true;

        final Exchange exchange = pair.getExchange();
        Processor processor = pair.getProcessor();
        Producer producer = pair.getProducer();

        TracedRouteNodes traced = exchange.getUnitOfWork() != null ? exchange.getUnitOfWork().getTracedRouteNodes() : null;

        // compute time taken if sending to another endpoint
        StopWatch watch = null;
        if (producer != null) {
            watch = new StopWatch();
        }

        try {
            // prepare tracing starting from a new block
            if (traced != null) {
                traced.pushBlock();
            }

            // let the prepared process it, remember to begin the exchange pair
            AsyncProcessor async = AsyncProcessorTypeConverter.convert(processor);
            pair.begin();
            sync = AsyncProcessorHelper.process(async, exchange, new AsyncCallback() {
                public void done(boolean doneSync) {
                    // we are done with the exchange pair
                    pair.done();

                    // we only have to handle async completion of the routing slip
                    if (doneSync) {
                        return;
                    }

                    // continue processing the multicast asynchronously
                    Exchange subExchange = exchange;

                    // Decide whether to continue with the multicast or not; similar logic to the Pipeline
                    // remember to test for stop on exception and aggregate before copying back results
                    boolean continueProcessing = PipelineHelper.continueProcessing(subExchange, "Sequential processing failed for number " + total.get(), LOG);
                    if (stopOnException && !continueProcessing) {
                        if (subExchange.getException() != null) {
                            // wrap in exception to explain where it failed
                            subExchange.setException(new CamelExchangeException("Sequential processing failed for number " + total, subExchange, subExchange.getException()));
                        } else {
                            // we want to stop on exception, and the exception was handled by the error handler
                            // this is similar to what the pipeline does, so we should do the same to not surprise end users
                            // so we should set the failed exchange as the result and be done
                            result.set(subExchange);
                        }
                        // and do the done work
                        doDone(original, subExchange, callback, false);
                        return;
                    }

                    try {
                        doAggregate(getAggregationStrategy(subExchange), result, subExchange);
                    } catch (Throwable e) {
                        // wrap in exception to explain where it failed
                        subExchange.setException(new CamelExchangeException("Sequential processing failed for number " + total, subExchange, e));
                        // and do the done work
                        doDone(original, subExchange, callback, false);
                        return;
                    }

                    total.incrementAndGet();

                    // maybe there are more processors to multicast
                    while (it.hasNext()) {

                        // prepare and run the next
                        ProcessorExchangePair pair = it.next();
                        subExchange = pair.getExchange();
                        updateNewExchange(subExchange, total.get(), pairs, it);
                        boolean sync = doProcessSequential(original, result, pairs, it, pair, callback, total);

                        if (!sync) {
                            if (LOG.isTraceEnabled()) {
                                LOG.trace("Processing exchangeId: " + original.getExchangeId() + " is continued being processed asynchronously");
                            }
                            return;
                        }

                        // Decide whether to continue with the multicast or not; similar logic to the Pipeline
                        // remember to test for stop on exception and aggregate before copying back results
                        continueProcessing = PipelineHelper.continueProcessing(subExchange, "Sequential processing failed for number " + total.get(), LOG);
                        if (stopOnException && !continueProcessing) {
                            if (subExchange.getException() != null) {
                                // wrap in exception to explain where it failed
                                subExchange.setException(new CamelExchangeException("Sequential processing failed for number " + total, subExchange, subExchange.getException()));
                            } else {
                                // we want to stop on exception, and the exception was handled by the error handler
                                // this is similar to what the pipeline does, so we should do the same to not surprise end users
                                // so we should set the failed exchange as the result and be done
                                result.set(subExchange);
                            }
                            // and do the done work
                            doDone(original, subExchange, callback, false);
                            return;
                        }

                        try {
                            doAggregate(getAggregationStrategy(subExchange), result, subExchange);
                        } catch (Throwable e) {
                            // wrap in exception to explain where it failed
                            subExchange.setException(new CamelExchangeException("Sequential processing failed for number " + total, subExchange, e));
                            // and do the done work
                            doDone(original, subExchange, callback, false);
                            return;
                        }

                        total.incrementAndGet();
                    }

                    // do the done work
                    subExchange = result.get() != null ? result.get() : null;
                    doDone(original, subExchange, callback, false);
                }
            });
        } finally {
            // pop the block so by next round we have the same staring point and thus the tracing looks accurate
            if (traced != null) {
                traced.popBlock();
            }
            if (producer != null) {
                long timeTaken = watch.stop();
                Endpoint endpoint = producer.getEndpoint();
                // emit event that the exchange was sent to the endpoint
                EventHelper.notifyExchangeSent(exchange.getContext(), exchange, endpoint, timeTaken);
            }
        }

        return sync;
    }
    protected void doDone(Exchange original, Exchange subExchange, AsyncCallback callback, boolean doneSync) {
        // cleanup any per exchange aggregation strategy
        removeAggregationStrategyFromExchange(original);
        if (original.getException() != null) {
            // multicast uses error handling on its output processors and they have tried to redeliver
            // so we shall signal back to the other error handlers that we are exhausted and they should not
            // also try to redeliver as we will then do that twice
            original.setProperty(Exchange.REDELIVERY_EXHAUSTED, Boolean.TRUE);
        }
        if (subExchange != null) {
            // and copy the current result to original so it will contain this exception
            ExchangeHelper.copyResults(original, subExchange);
        }
        callback.done(doneSync);
    }
    public boolean process(Exchange exchange, AsyncCallback callback) {
        final AtomicExchange result = new AtomicExchange();
        final Iterable<ProcessorExchangePair> pairs;

        // multicast uses fine grained error handling on the output processors
        // so use try .. catch to cater for this
        try {
            boolean sync = true;

            pairs = createProcessorExchangePairs(exchange);
            if (isParallelProcessing()) {
                // ensure an executor is set when running in parallel
                ObjectHelper.notNull(executorService, "executorService", this);
                doProcessParallel(exchange, result, pairs, isStreaming(), callback);
            } else {
                sync = doProcessSequential(exchange, result, pairs, callback);
            }

            if (!sync) {
                // the remainder of the multicast will be completed async
                // so we break out now, then the callback will be invoked which then continue routing from where we left here
                return false;
            }
        } catch (Throwable e) {
            exchange.setException(e);
            // and do the done work
            doDone(exchange, null, callback, true);
            return true;
        }

        // multicasting was processed successfully
        // and do the done work
        Exchange subExchange = result.get() != null ? result.get() : null;
        doDone(exchange, subExchange, callback, true);
        return true;
    }
