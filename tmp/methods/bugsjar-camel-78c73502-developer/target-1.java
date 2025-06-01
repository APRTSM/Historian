    protected boolean doProcessSequential(Exchange original, AtomicExchange result, Iterable<ProcessorExchangePair> pairs, AsyncCallback callback) throws Exception {
        AtomicInteger total = new AtomicInteger();
        Iterator<ProcessorExchangePair> it = pairs.iterator();

        while (it.hasNext()) {
            ProcessorExchangePair pair = it.next();
            Exchange subExchange = pair.getExchange();
            updateNewExchange(subExchange, total.get(), pairs, it);

            boolean sync = doProcessSequential(original, result, pairs, it, pair, callback, total);
            if (!sync) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Processing exchangeId: {} is continued being processed asynchronously", pair.getExchange().getExchangeId());
                }
                // the remainder of the multicast will be completed async
                // so we break out now, then the callback will be invoked which then continue routing from where we left here
                return false;
            }

            if (LOG.isTraceEnabled()) {
                LOG.trace("Processing exchangeId: {} is continued being processed synchronously", pair.getExchange().getExchangeId());
            }

            // Decide whether to continue with the multicast or not; similar logic to the Pipeline
            // remember to test for stop on exception and aggregate before copying back results
            boolean continueProcessing = PipelineHelper.continueProcessing(subExchange, "Sequential processing failed for number " + total.get(), LOG);
            if (stopOnException && !continueProcessing) {
                if (subExchange.getException() != null) {
                    // wrap in exception to explain where it failed
                    CamelExchangeException cause = new CamelExchangeException("Sequential processing failed for number " + total.get(), subExchange, subExchange.getException());
                    subExchange.setException(cause);
                }
                // we want to stop on exception, and the exception was handled by the error handler
                // this is similar to what the pipeline does, so we should do the same to not surprise end users
                // so we should set the failed exchange as the result and be done
                result.set(subExchange);
                return true;
            }

            LOG.trace("Sequential processing complete for number {} exchange: {}", total, subExchange);

            doAggregate(getAggregationStrategy(subExchange), result, subExchange);
            total.incrementAndGet();
        }

        LOG.debug("Done sequential processing {} exchanges", total);

        return true;
    }
    public boolean process(Exchange exchange, AsyncCallback callback) {
        final AtomicExchange result = new AtomicExchange();
        final Iterable<ProcessorExchangePair> pairs;

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
            // unexpected exception was thrown, maybe from iterator etc. so do not regard as exhausted
            // and do the done work
            doDone(exchange, null, callback, true, false);
            return true;
        }

        // multicasting was processed successfully
        // and do the done work
        Exchange subExchange = result.get() != null ? result.get() : null;
        doDone(exchange, subExchange, callback, true, true);
        return true;
    }
    protected void doProcessParallel(final Exchange original, final AtomicExchange result, final Iterable<ProcessorExchangePair> pairs,
                                     final boolean streaming, final AsyncCallback callback) throws Exception {

        ObjectHelper.notNull(executorService, "ExecutorService", this);
        ObjectHelper.notNull(aggregateExecutorService, "AggregateExecutorService", this);

        final CompletionService<Exchange> completion;
        if (streaming) {
            // execute tasks in parallel+streaming and aggregate in the order they are finished (out of order sequence)
            completion = new ExecutorCompletionService<Exchange>(executorService);
        } else {
            // execute tasks in parallel and aggregate in the order the tasks are submitted (in order sequence)
            completion = new SubmitOrderedCompletionService<Exchange>(executorService);
        }

        final AtomicInteger total = new AtomicInteger(0);
        final Iterator<ProcessorExchangePair> it = pairs.iterator();

        if (it.hasNext()) {
            // when parallel then aggregate on the fly
            final AtomicBoolean running = new AtomicBoolean(true);
            final AtomicBoolean allTasksSubmitted = new AtomicBoolean();
            final CountDownLatch aggregationOnTheFlyDone = new CountDownLatch(1);
            final AtomicException executionException = new AtomicException();

            // issue task to execute in separate thread so it can aggregate on-the-fly
            // while we submit new tasks, and those tasks complete concurrently
            // this allows us to optimize work and reduce memory consumption
            final AggregateOnTheFlyTask aggregateOnTheFlyTask = new AggregateOnTheFlyTask(result, original, total, completion, running,
                    aggregationOnTheFlyDone, allTasksSubmitted, executionException);
            final AtomicBoolean aggregationTaskSubmitted = new AtomicBoolean();

            LOG.trace("Starting to submit parallel tasks");

            while (it.hasNext()) {
                final ProcessorExchangePair pair = it.next();
                final Exchange subExchange = pair.getExchange();
                updateNewExchange(subExchange, total.intValue(), pairs, it);

                completion.submit(new Callable<Exchange>() {
                    public Exchange call() throws Exception {
                        // only start the aggregation task when the task is being executed to avoid staring
                        // the aggregation task to early and pile up too many threads
                        if (aggregationTaskSubmitted.compareAndSet(false, true)) {
                            // but only submit the task once
                            aggregateExecutorService.submit(aggregateOnTheFlyTask);
                        }

                        if (!running.get()) {
                            // do not start processing the task if we are not running
                            return subExchange;
                        }

                        try {
                            doProcessParallel(pair);
                        } catch (Throwable e) {
                            subExchange.setException(e);
                        }

                        // Decide whether to continue with the multicast or not; similar logic to the Pipeline
                        Integer number = getExchangeIndex(subExchange);
                        boolean continueProcessing = PipelineHelper.continueProcessing(subExchange, "Parallel processing failed for number " + number, LOG);
                        if (stopOnException && !continueProcessing) {
                            // signal to stop running
                            running.set(false);
                            // throw caused exception
                            if (subExchange.getException() != null) {
                                // wrap in exception to explain where it failed
                                CamelExchangeException cause = new CamelExchangeException("Parallel processing failed for number " + number, subExchange, subExchange.getException());
                                subExchange.setException(cause);
                            }
                        }

                        LOG.trace("Parallel processing complete for exchange: {}", subExchange);
                        return subExchange;
                    }
                });

                total.incrementAndGet();
            }

            // signal all tasks has been submitted
            LOG.trace("Signaling that all {} tasks has been submitted.", total.get());
            allTasksSubmitted.set(true);

            // its to hard to do parallel async routing so we let the caller thread be synchronously
            // and have it pickup the replies and do the aggregation (eg we use a latch to wait)
            // wait for aggregation to be done
            LOG.debug("Waiting for on-the-fly aggregation to complete aggregating {} responses for exchangeId: {}", total.get(), original.getExchangeId());
            aggregationOnTheFlyDone.await();

            // did we fail for whatever reason, if so throw that caused exception
            if (executionException.get() != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Parallel processing failed due {}", executionException.get().getMessage());
                }
                throw executionException.get();
            }
        }

        // no everything is okay so we are done
        LOG.debug("Done parallel processing {} exchanges", total);
    }
        protected boolean processNext(final Exchange exchange, final AsyncCallback callback) {
            final Exception caught = exchange.getException();
            if (caught == null) {
                return true;
            }

            // store the last to endpoint as the failure endpoint
            if (exchange.getProperty(Exchange.FAILURE_ENDPOINT) == null) {
                exchange.setProperty(Exchange.FAILURE_ENDPOINT, exchange.getProperty(Exchange.TO_ENDPOINT));
            }
            // give the rest of the pipeline another chance
            exchange.setProperty(Exchange.EXCEPTION_CAUGHT, caught);
            exchange.setException(null);
            // and we should not be regarded as exhausted as we are in a try .. catch block
            exchange.removeProperty(Exchange.REDELIVERY_EXHAUSTED);

            // is the exception handled by the catch clause
            final Boolean handled = catchClause.handles(exchange);

            if (LOG.isDebugEnabled()) {
                LOG.debug("The exception is handled: {} for the exception: {} caused by: {}",
                        new Object[]{handled, caught.getClass().getName(), caught.getMessage()});
            }

            boolean sync = super.processNext(exchange, new AsyncCallback() {
                public void done(boolean doneSync) {
                    // we only have to handle async completion of the pipeline
                    if (doneSync) {
                        return;
                    }

                    if (!handled) {
                        if (exchange.getException() == null) {
                            exchange.setException(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class));
                        }
                    }
                    // always clear redelivery exhausted in a catch clause
                    exchange.removeProperty(Exchange.REDELIVERY_EXHAUSTED);

                    // signal callback to continue routing async
                    ExchangeHelper.prepareOutToIn(exchange);
                    callback.done(false);
                }
            });

            if (sync) {
                // set exception back on exchange
                if (!handled) {
                    if (exchange.getException() == null) {
                        exchange.setException(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class));
                    }
                }
                // always clear redelivery exhausted in a catch clause
                exchange.removeProperty(Exchange.REDELIVERY_EXHAUSTED);
            }

            return sync;
        }
