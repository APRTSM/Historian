    public void error(Marker marker, Message msg) {
        if (isEnabled(Level.ERROR, marker, msg, null)) {
            log(marker, FQCN, Level.ERROR, msg, null);
        }
    }
    public void fatal(Marker marker, Message msg) {
        if (isEnabled(Level.FATAL, marker, msg, null)) {
            log(marker, FQCN, Level.FATAL, msg, null);
        }
    }
