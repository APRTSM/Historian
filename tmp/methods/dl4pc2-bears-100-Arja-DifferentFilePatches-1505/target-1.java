    protected Collection<Event> analyzePosition(Position position) {
        Device device = Context.getIdentityManager().getDeviceById(position.getDeviceId());
        if (device == null) {
            return null;
        }
        Collection<Event> result = null;

        boolean ignition = position.getBoolean(Position.KEY_IGNITION);

        boolean oldIgnition = false;
        Position lastPosition = Context.getIdentityManager().getLastPosition(position.getDeviceId());
        if (lastPosition != null) {
            oldIgnition = lastPosition.getBoolean(Position.KEY_IGNITION);
        }

        return result;
    }
    public void setPositionId(long positionId) {
    }
