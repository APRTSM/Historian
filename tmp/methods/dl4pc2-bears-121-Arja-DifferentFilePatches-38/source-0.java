    public void setDeviceTime(Date deviceTime) {
        if (deviceTime != null) {
            this.deviceTime = new Date(deviceTime.getTime());
        } else {
            this.deviceTime = null;
        }
    }
    public void setValid(boolean valid) {
        this.valid = valid;
    }
