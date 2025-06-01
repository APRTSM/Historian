    private synchronized Configuration setConfiguration(final Configuration config) {
        if (config == null) {
            throw new NullPointerException("No Configuration was provided");
        }
        final Configuration prev = this.config;
        config.addListener(this);
        final ConcurrentMap<String, String> map = config.getComponent(Configuration.CONTEXT_PROPERTIES);
        map.putIfAbsent("hostName", NetUtils.getLocalHostname());
        map.putIfAbsent("contextName", name);
        config.start();
        this.config = config;
        updateLoggers();
        if (prev != null) {
            prev.removeListener(this);
            prev.stop();
        }

        // notify listeners
        final PropertyChangeEvent evt = new PropertyChangeEvent(this, PROPERTY_CONFIG, prev, config);
        for (final PropertyChangeListener listener : propertyChangeListeners) {
            listener.propertyChange(evt);
        }
        return prev;
    }
