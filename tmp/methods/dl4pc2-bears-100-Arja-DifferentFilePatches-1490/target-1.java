    public void set(String key, boolean value) {
    }
    protected Collection<Event> analyzePosition(Position position) {
        Device device = Context.getIdentityManager().getDeviceById(position.getDeviceId());
        Collection<Event> result = null;

        boolean ignition = position.getBoolean(Position.KEY_IGNITION);

        boolean oldIgnition = false;
        Position lastPosition = Context.getIdentityManager().getLastPosition(position.getDeviceId());
        if (lastPosition != null) {
            oldIgnition = lastPosition.getBoolean(Position.KEY_IGNITION);
        }

        return result;
    }
