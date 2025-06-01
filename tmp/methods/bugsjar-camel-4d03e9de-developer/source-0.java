    public boolean process(final Exchange exchange, final AsyncCallback callback) {
        WaitForTaskToComplete wait = waitForTaskToComplete;
        if (exchange.getProperty(Exchange.ASYNC_WAIT) != null) {
            wait = exchange.getProperty(Exchange.ASYNC_WAIT, WaitForTaskToComplete.class);
        }

        if (wait == WaitForTaskToComplete.Always
            || (wait == WaitForTaskToComplete.IfReplyExpected && ExchangeHelper.isOutCapable(exchange))) {

            // do not handover the completion as we wait for the copy to complete, and copy its result back when it done
            Exchange copy = prepareCopy(exchange, false);

            // latch that waits until we are complete
            final CountDownLatch latch = new CountDownLatch(1);

            // we should wait for the reply so install a on completion so we know when its complete
            copy.addOnCompletion(new SynchronizationAdapter() {
                @Override
                public void onDone(Exchange response) {
                    // check for timeout, which then already would have invoked the latch
                    if (latch.getCount() == 0) {
                        if (log.isTraceEnabled()) {
                            log.trace("{}. Timeout occurred so response will be ignored: {}", this, response.hasOut() ? response.getOut() : response.getIn());
                        }
                        return;
                    } else {
                        if (log.isTraceEnabled()) {
                            log.trace("{} with response: {}", this, response.hasOut() ? response.getOut() : response.getIn());
                        }
                        try {
                            ExchangeHelper.copyResults(exchange, response);
                        } finally {
                            // always ensure latch is triggered
                            latch.countDown();
                        }
                    }
                }

                @Override
                public boolean allowHandover() {
                    // do not allow handover as we want to seda producer to have its completion triggered
                    // at this point in the routing (at this leg), instead of at the very last (this ensure timeout is honored)
                    return false;
                }

                @Override
                public String toString() {
                    return "onDone at endpoint: " + endpoint;
                }
            });

            log.trace("Adding Exchange to queue: {}", copy);
            try {
                addToQueue(copy);
            } catch (SedaConsumerNotAvailableException e) {
                exchange.setException(e);
                callback.done(true);
                return true;
            }

            if (timeout > 0) {
                if (log.isTraceEnabled()) {
                    log.trace("Waiting for task to complete using timeout (ms): {} at [{}]", timeout, endpoint.getEndpointUri());
                }
                // lets see if we can get the task done before the timeout
                boolean done = false;
                try {
                    done = latch.await(timeout, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    // ignore
                }
                if (!done) {
                    exchange.setException(new ExchangeTimedOutException(exchange, timeout));
                    // remove timed out Exchange from queue
                    endpoint.getQueue().remove(copy);
                    // count down to indicate timeout
                    latch.countDown();
                }
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("Waiting for task to complete (blocking) at [{}]", endpoint.getEndpointUri());
                }
                // no timeout then wait until its done
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        } else {
            // no wait, eg its a InOnly then just add to queue and return
            // handover the completion so its the copy which performs that, as we do not wait
            Exchange copy = prepareCopy(exchange, true);
            log.trace("Adding Exchange to queue: {}", copy);
            try {
                addToQueue(copy);
            } catch (SedaConsumerNotAvailableException e) {
                exchange.setException(e);
                callback.done(true);
                return true;
            }
        }

        // we use OnCompletion on the Exchange to callback and wait for the Exchange to be done
        // so we should just signal the callback we are done synchronously
        callback.done(true);
        return true;
    }
    protected void addToQueue(Exchange exchange) throws SedaConsumerNotAvailableException {
        BlockingQueue<Exchange> queue = null;
        QueueReference queueReference = endpoint.getQueueReference();
        if (queueReference != null) {
            queue = queueReference.getQueue();
        }
        if (queue == null) {
            throw new SedaConsumerNotAvailableException("No queue available on endpoint: " + endpoint, exchange);
        }

        boolean empty = !queueReference.hasConsumers();
        if (empty) {
            if (endpoint.isFailIfNoConsumers()) {
                throw new SedaConsumerNotAvailableException("No consumers available on endpoint: " + endpoint, exchange);
            } else if (endpoint.isDiscardIfNoConsumers()) {
                log.debug("Discard message as no active consumers on endpoint: " + endpoint);
                return;
            }
        }

        if (blockWhenFull) {
            try {
                queue.put(exchange);
            } catch (InterruptedException e) {
                // ignore
                log.debug("Put interrupted, are we stopping? {}", isStopping() || isStopped());
            }
        } else {
            queue.add(exchange);
        }
    }
