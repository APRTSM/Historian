    public Producer createProducer() throws Exception {
        return new SedaProducer(this, getQueue(), getWaitForTaskToComplete(), getTimeout(), isBlockWhenFull());
    }
