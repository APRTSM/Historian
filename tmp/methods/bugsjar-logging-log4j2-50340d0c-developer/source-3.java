    private synchronized Configuration setConfiguration(final Configuration config) {
        if (config == null) {
            throw new NullPointerException("No Configuration was provided");
        }
        final Configuration prev = this.config;
        config.addListener(this);
        final Map<String, String> map = new HashMap<String, String>();
        map.put("hostName", NetUtils.getLocalHostname());
        map.put("contextName", name);
        config.addComponent(Configuration.CONTEXT_PROPERTIES, map);
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
    public Map<String, String> getProperties() {
        return (Map<String, String>) componentMap.get(CONTEXT_PROPERTIES);
    }
    protected BaseConfiguration() {
        pluginManager = new PluginManager("Core");
        rootNode = new Node();
    }
    public static StrLookup configureSubstitutor(@PluginElement("Properties") final Property[] properties,
                                                 @PluginConfiguration final Configuration config) {
        if (properties == null) {
            return new Interpolator(null);
        }
        final Map<String, String> map = new HashMap<String, String>(config.getProperties());

        for (final Property prop : properties) {
            map.put(prop.getName(), prop.getValue());
        }

        return new Interpolator(new MapLookup(map));
    }
    public Interpolator() {
        this.defaultLookup = new MapLookup(new HashMap<String, String>());
        lookups.put("sys", new SystemPropertiesLookup());
        lookups.put("env", new EnvironmentLookup());
        lookups.put("jndi", new JndiLookup());
        try {
            if (Class.forName("javax.servlet.ServletContext") != null) {
                lookups.put("web", new WebLookup());
            }
        } catch (ClassNotFoundException ex) {
            LOGGER.debug("ServletContext not present - WebLookup not added");
        } catch (Exception ex) {
            LOGGER.error("Unable to locate ServletContext", ex);
        }
    }
