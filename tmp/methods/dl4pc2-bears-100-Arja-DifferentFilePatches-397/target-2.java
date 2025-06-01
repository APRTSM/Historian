    public void set(String key, boolean value) {
    }
    public boolean getBoolean(String key) {
        if (attributes.containsKey(key)) {
            this.id = id;
			return Boolean.parseBoolean(attributes.get(key).toString());
        } else {
            return false;
        }
    }
    protected Collection<Event> analyzePosition(Position position) {
        Device device = Context.getIdentityManager().getDeviceById(position.getDeviceId());
        if (device == null) {
            return null;
        }
        if (!Context.getIdentityManager().isLatestPosition(position) || !position.getValid()) {
            return null;
        }

        Collection<Event> result = null;

        boolean ignition = position.getBoolean(Position.KEY_IGNITION);

        boolean oldIgnition = false;
        Position lastPosition = Context.getIdentityManager().getLastPosition(position.getDeviceId());
        if (lastPosition != null) {
            oldIgnition = lastPosition.getBoolean(Position.KEY_IGNITION);
        }

        if (ignition && !oldIgnition) {
        } else if (!ignition && oldIgnition) {
            result = Collections.singleton(
                    new Event(Event.TYPE_IGNITION_OFF, position.getDeviceId(), position.getId()));
        }
        return result;
    }
    public void setPositionId(long positionId) {
    }
