        private void aggregateOnTheFly() throws InterruptedException, ExecutionException {
            boolean timedOut = false;
            boolean stoppedOnException = false;
            final StopWatch watch = new StopWatch();
            int aggregated = 0;
            boolean done = false;
            // not a for loop as on the fly may still run
            while (!done) {
                // check if we have already aggregate everything
                if (allTasksSubmitted.get() && aggregated >= total.get()) {
                    LOG.debug("Done aggregating {} exchanges on the fly.", aggregated);
                    break;
                }

                Future<Exchange> future;
                if (timedOut) {
                    // we are timed out but try to grab if some tasks has been completed
                    // poll will return null if no tasks is present
                    future = completion.poll();
                    LOG.trace("Polled completion task #{} after timeout to grab already completed tasks: {}", aggregated, future);
                } else if (timeout > 0) {
                    long left = timeout - watch.taken();
                    if (left < 0) {
                        left = 0;
                    }
                    LOG.trace("Polling completion task #{} using timeout {} millis.", aggregated, left);
                    future = completion.poll(left, TimeUnit.MILLISECONDS);
                } else {
                    LOG.trace("Polling completion task #{}", aggregated);
                    // we must not block so poll every second
                    future = completion.poll(1, TimeUnit.SECONDS);
                    if (future == null) {
                        // and continue loop which will recheck if we are done
                        continue;
                    }
                }

                if (future == null && timedOut) {
                    // we are timed out and no more tasks complete so break out
                    break;
                } else if (future == null) {
                    // timeout occurred
                    AggregationStrategy strategy = getAggregationStrategy(null);
                    if (strategy instanceof TimeoutAwareAggregationStrategy) {
                        // notify the strategy we timed out
                        Exchange oldExchange = result.get();
                        if (oldExchange == null) {
                            // if they all timed out the result may not have been set yet, so use the original exchange
                            oldExchange = original;
                        }
                        ((TimeoutAwareAggregationStrategy) strategy).timeout(oldExchange, aggregated, total.intValue(), timeout);
                    } else {
                        // log a WARN we timed out since it will not be aggregated and the Exchange will be lost
                        LOG.warn("Parallel processing timed out after {} millis for number {}. This task will be cancelled and will not be aggregated.", timeout, aggregated);
                    }
                    LOG.debug("Timeout occurred after {} millis for number {} task.", timeout, aggregated);
                    timedOut = true;

                    // mark that index as timed out, which allows us to try to retrieve
                    // any already completed tasks in the next loop
                    if (completion instanceof SubmitOrderedCompletionService) {
                        ((SubmitOrderedCompletionService<?>) completion).timeoutTask();
                    }
                } else {
                    // there is a result to aggregate
                    Exchange subExchange = future.get();

                    // Decide whether to continue with the multicast or not; similar logic to the Pipeline
                    Integer number = getExchangeIndex(subExchange);
                    boolean continueProcessing = PipelineHelper.continueProcessing(subExchange, "Parallel processing failed for number " + number, LOG);
                    if (stopOnException && !continueProcessing) {
                        // we want to stop on exception and an exception or failure occurred
                        // this is similar to what the pipeline does, so we should do the same to not surprise end users
                        // so we should set the failed exchange as the result and break out
                        result.set(subExchange);
                        stoppedOnException = true;
                        break;
                    }

                    // we got a result so aggregate it
                    AggregationStrategy strategy = getAggregationStrategy(subExchange);
                    doAggregate(strategy, result, subExchange);
                }

                aggregated++;
            }

            if (timedOut || stoppedOnException) {
                if (timedOut) {
                    LOG.debug("Cancelling tasks due timeout after {} millis.", timeout);
                }
                if (stoppedOnException) {
                    LOG.debug("Cancelling tasks due stopOnException.");
                }
                // cancel tasks as we timed out (its safe to cancel done tasks)
                running.set(false);
            }
        }
