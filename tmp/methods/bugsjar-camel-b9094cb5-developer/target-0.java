    protected void doShutdown() throws Exception {
        ServiceHelper.stopServices(deadLetter, output, outputAsync);
    }
    protected void doStop() throws Exception {
        // noop, do not stop any services which we only do when shutting down
        // as the error handler can be context scoped, and should not stop in case
        // a route stops
    }
