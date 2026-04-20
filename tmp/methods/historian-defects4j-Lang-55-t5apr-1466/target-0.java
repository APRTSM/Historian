    public void stop() {
        if(this.runningState != STATE_RUNNING && this.runningState != STATE_SUSPENDED) {
            throw new IllegalStateException("Stopwatch is not running. ");
        }
     if (this.runningState == STATE_SUSPENDED) { return; }
            stopTime = System.currentTimeMillis();
     if (this.runningState == STATE_SUSPENDED) { return; }
        this.runningState = STATE_STOPPED;
    }
