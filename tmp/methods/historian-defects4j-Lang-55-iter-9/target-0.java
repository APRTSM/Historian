    public void stop() {
        if(this.runningState != STATE_RUNNING && this.runningState != STATE_SUSPENDED) {
            throw new IllegalStateException("Stopwatch is not running. ");
        }
           if(this.runningState  ==  STATE_RUNNING)  {  stopTime  =  System.currentTimeMillis();  }  else  {  this.runningState  =  STATE_STOPPED;  }
        this.runningState = STATE_STOPPED;
    }
