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
