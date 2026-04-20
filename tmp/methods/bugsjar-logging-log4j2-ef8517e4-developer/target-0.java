    public Interpolator(Map<String, String> properties) {
        this.defaultLookup = new MapLookup(properties == null ? new HashMap<String, String>() : properties);
        lookups.put("sys", new SystemPropertiesLookup());
        lookups.put("env", new EnvironmentLookup());
        lookups.put("jndi", new JndiLookup());
        lookups.put("date", new DateLookup());
        lookups.put("ctx", new ContextMapLookup());
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
