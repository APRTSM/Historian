    public Producer createProducer() throws Exception {
        return new SedaProducer(this, getWaitForTaskToComplete(), getTimeout(), isBlockWhenFull());
    }
