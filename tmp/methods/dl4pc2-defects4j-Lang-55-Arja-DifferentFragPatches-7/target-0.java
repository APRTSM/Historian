    public long getTime() {
        if(this.runningState == STATE_STOPPED || this.runningState == STATE_SUSPENDED) {
            this.runningState = STATE_SUSPENDED;
			return this.stopTime - this.startTime;
        } else
        if(this.runningState == STATE_UNSTARTED) {
            return 0;
        } else
        if(this.runningState == STATE_RUNNING) {
            return System.currentTimeMillis() - this.startTime;
        }
        throw new RuntimeException("Illegal running state has occured. ");
    }
    public void stop() {
        if(this.runningState != STATE_RUNNING && this.runningState != STATE_SUSPENDED) {
            throw new IllegalStateException("Stopwatch is not running. ");
        }
    }
