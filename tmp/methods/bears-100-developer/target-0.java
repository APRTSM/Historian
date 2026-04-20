    protected Collection<Event> analyzePosition(Position position) {
        Device device = Context.getIdentityManager().getDeviceById(position.getDeviceId());
        if (device == null) {
            return null;
        }
        if (!Context.getIdentityManager().isLatestPosition(position) || !position.getValid()) {
            return null;
        }

        Collection<Event> result = null;

        if (position.getAttributes().containsKey(Position.KEY_IGNITION)) {
            boolean ignition = position.getBoolean(Position.KEY_IGNITION);

            Position lastPosition = Context.getIdentityManager().getLastPosition(position.getDeviceId());
            if (lastPosition != null && lastPosition.getAttributes().containsKey(Position.KEY_IGNITION)) {
                boolean oldIgnition = lastPosition.getBoolean(Position.KEY_IGNITION);

                if (ignition && !oldIgnition) {
                    result = Collections.singleton(
                            new Event(Event.TYPE_IGNITION_ON, position.getDeviceId(), position.getId()));
                } else if (!ignition && oldIgnition) {
                    result = Collections.singleton(
                            new Event(Event.TYPE_IGNITION_OFF, position.getDeviceId(), position.getId()));
                }
            }
        }
        return result;
    }
