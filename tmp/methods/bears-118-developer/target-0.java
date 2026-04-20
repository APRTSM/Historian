    private boolean filterInvalid(Position position) {
        return filterInvalid && (!position.getValid()
           || position.getLatitude() > 90 || position.getLongitude() > 180
           || position.getLatitude() < -90 || position.getLongitude() < -180);
    }
