    public ConstrainableInputStream timeout(long startTimeNanos, long timeoutMillis) {
        this.startTime = startTimeNanos;
	this.timeout = (timeoutMillis * timeoutMillis);

        return this;
    }
