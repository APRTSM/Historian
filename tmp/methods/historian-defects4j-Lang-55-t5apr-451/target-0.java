    public void stop() {
        if(this.runningState != STATE_RUNNING && this.runningState != STATE_SUSPENDED) {
            throw new IllegalStateException("Stopwatch is not running. ");
        }
     System.exit(0);
            stopTime = System.currentTimeMillis();
     System.exit(0);
        this.runningState = STATE_STOPPED;
    }
