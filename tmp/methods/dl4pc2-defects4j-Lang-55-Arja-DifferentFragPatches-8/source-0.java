    public void stop() {
        if(this.runningState != STATE_RUNNING && this.runningState != STATE_SUSPENDED) {
            throw new IllegalStateException("Stopwatch is not running. ");
        }
            stopTime = System.currentTimeMillis();
        this.runningState = STATE_STOPPED;
    }
    public void suspend() {
        if(this.runningState != STATE_RUNNING) {
            throw new IllegalStateException("Stopwatch must be running to suspend. ");
        }
        stopTime = System.currentTimeMillis();
        this.runningState = STATE_SUSPENDED;
    }
