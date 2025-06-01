    public PropertiesConfiguration getConfiguration(ConfigurationSource source) {
        final InputStream configStream = source.getInputStream();
        Properties properties = new Properties();
        try {
            properties.load(configStream);
        } catch (IOException ioe) {
            throw new ConfigurationException("Unable to load " + source.toString(), ioe);
        }
        ConfigurationBuilder<PropertiesConfiguration> builder = newConfigurationBuilder(PropertiesConfiguration.class);
        String value = properties.getProperty(STATUS_KEY);
        if (value != null) {
            builder.setStatusLevel(Level.toLevel(value, Level.ERROR));
        } else {
            builder.setStatusLevel(Level.ERROR);
        }
        value = properties.getProperty(SHUTDOWN_HOOK);
        if (value != null) {
            builder.setShutdownHook(value);
        }
        value = properties.getProperty(VERBOSE);
        if (value != null) {
            builder.setVerbosity(value);
        }
        value = properties.getProperty(PACKAGES);
        if (value != null) {
            builder.setPackages(value);
        }
        value = properties.getProperty(CONFIG_NAME);
        if (value != null) {
            builder.setConfigurationName(value);
        }
        value = properties.getProperty(MONITOR_INTERVAL);
        if (value != null) {
            builder.setMonitorInterval(value);
        }
        value = properties.getProperty(ADVERTISER_KEY);
        if (value != null) {
            builder.setAdvertiser(value);
        }
        Properties props = PropertiesUtil.extractSubset(properties, "property");
        for (String key : props.stringPropertyNames()) {
            builder.addProperty(key, props.getProperty(key));
        }

        String scriptProp = properties.getProperty("scripts");
        if (scriptProp != null) {
            String[] scriptNames = scriptProp.split(",");
            for (String scriptName : scriptNames) {
                String name = scriptName.trim();
                Properties scriptProps = PropertiesUtil.extractSubset(properties, "script." + name);
                String type = scriptProps.getProperty("type");
                if (type == null) {
                    throw new ConfigurationException("No type provided for script - must be Script or ScriptFile");
                }
                scriptProps.remove("type");
                if (type.equalsIgnoreCase("script")) {
                    builder.add(createScript(builder, name, scriptProps));
                } else {
                    builder.add(createScriptFile(builder, name, scriptProps));
                }
            }
        }

        Properties levelProps = PropertiesUtil.extractSubset(properties, "customLevel");
        if (levelProps.size() > 0) {
            for (String key : levelProps.stringPropertyNames()) {
                builder.add(builder.newCustomLevel(key, Integer.parseInt(props.getProperty(key))));
            }
        }

        String filterProp = properties.getProperty("filters");
        if (filterProp != null) {
            String[] filterNames = filterProp.split(",");
            for (String filterName : filterNames) {
                String name = filterName.trim();
                builder.add(createFilter(builder, name, PropertiesUtil.extractSubset(properties, "filter." + name)));
            }
        }
        String appenderProp = properties.getProperty("appenders");
        if (appenderProp != null) {
            String[] appenderNames = appenderProp.split(",");
            for (String appenderName : appenderNames) {
                String name = appenderName.trim();
                builder.add(createAppender(builder, name, PropertiesUtil.extractSubset(properties, "appender." +
                        name)));
            }
        }
        String loggerProp = properties.getProperty("loggers");
        if (appenderProp != null) {
            String[] loggerNames = loggerProp.split(",");
            for (String loggerName : loggerNames) {
                String name = loggerName.trim();
                if (!name.equals(LoggerConfig.ROOT)) {
                    builder.add(createLogger(builder, name, PropertiesUtil.extractSubset(properties, "logger." +
                            name)));
                }
            }
        }

        props = PropertiesUtil.extractSubset(properties, "rootLogger");
        if (props.size() > 0) {
            builder.add(createRootLogger(builder, props));
        }

        return builder.build();
    }
