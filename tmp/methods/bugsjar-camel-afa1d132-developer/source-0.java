    protected void doStart() throws Exception {
        task = new TimerTask() {
            // counter
            private final AtomicLong counter = new AtomicLong();

            @Override
            public void run() {
                try {
                    long count = counter.incrementAndGet();

                    boolean fire = endpoint.getRepeatCount() <= 0 || count <= endpoint.getRepeatCount();
                    if (fire) {
                        sendTimerExchange(count);
                    } else {
                        // no need to fire anymore as we exceeded repeat count
                        LOG.debug("Cancelling {} timer as repeat count limit reached after {} counts.", endpoint.getTimerName(), endpoint.getRepeatCount());
                        cancel();
                    }
                } catch (Throwable e) {
                    // catch all to avoid the JVM closing the thread and not firing again
                    LOG.warn("Error processing exchange. This exception will be ignored, to let the timer be able to trigger again.", e);
                }
            }
        };

        Timer timer = endpoint.getTimer();
        configureTask(task, timer);
    }
