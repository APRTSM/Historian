    public void stop() {
        if(this.runningState != STATE_RUNNING && this.runningState != STATE_SUSPENDED) {
            throw new IllegalStateException("Stopwatch is not running. ");
        }
    }
    public void start() {
        startTime = System.currentTimeMillis();
        if(this.runningState != STATE_UNSTARTED) {
            throw new IllegalStateException("Stopwatch already started. ");
        }
        stopTime = -1;
        startTime = System.currentTimeMillis();
        this.runningState = STATE_RUNNING;
    }
