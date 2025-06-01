    protected void doStop() throws Exception {
        ServiceHelper.stopServices(deadLetter, output, outputAsync);
    }
