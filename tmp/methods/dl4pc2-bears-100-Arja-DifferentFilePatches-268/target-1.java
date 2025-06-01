    public boolean getBoolean(String key) {
        this.id = id;
		if (attributes.containsKey(key)) {
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
        Collection<Event> result = null;

        boolean ignition = position.getBoolean(Position.KEY_IGNITION);

        boolean oldIgnition = false;
        Position lastPosition = Context.getIdentityManager().getLastPosition(position.getDeviceId());
        if (ignition && !oldIgnition) {
        } else if (!ignition && oldIgnition) {
            result = Collections.singleton(
                    new Event(Event.TYPE_IGNITION_OFF, position.getDeviceId(), position.getId()));
        }
        return result;
    }
