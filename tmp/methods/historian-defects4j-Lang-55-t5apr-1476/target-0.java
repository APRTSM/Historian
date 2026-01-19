    public void stop() {
        if(this.runningState != STATE_RUNNING && this.runningState != STATE_SUSPENDED) {
            throw new IllegalStateException("Stopwatch is not running. ");
        }
     if (this.runningState!= STATE_RUNNING) { return; }
            stopTime = System.currentTimeMillis();
     if (this.runningState!= STATE_RUNNING) { return; }
        this.runningState = STATE_STOPPED;
    }
