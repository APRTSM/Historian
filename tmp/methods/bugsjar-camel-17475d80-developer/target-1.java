    protected void doStop() throws Exception {
        if (future != null) {
            LOG.debug("This consumer is stopping, so cancelling scheduled task: " + future);
            future.cancel(true);
            future = null;
        }

        if (shutdownExecutor && scheduledExecutorService != null) {
            getCamelContext().getExecutorServiceManager().shutdownNow(scheduledExecutorService);
            scheduledExecutorService = null;
            future = null;
        }
    }
    protected void doStop() throws Exception {
        scheduler.unscheduleTask();
        ServiceHelper.stopAndShutdownServices(scheduler);

        // clear counters
        backoffCounter = 0;
        idleCounter = 0;
        errorCounter = 0;

        super.doStop();
    }
