    public PropertiesConfiguration build() {
        Map<String, String> rootProps = new HashMap<>();
        for (String key : rootProperties.stringPropertyNames()) {
            if (!key.contains(".")) {
                builder.addRootProperty(key, rootProperties.getProperty(key));
            }
        }
        builder
            .setStatusLevel(Level.toLevel(rootProperties.getProperty(STATUS_KEY), Level.ERROR))
            .setShutdownHook(rootProperties.getProperty(SHUTDOWN_HOOK))
            .setVerbosity(rootProperties.getProperty(VERBOSE))
            .setPackages(rootProperties.getProperty(PACKAGES))
            .setConfigurationName(rootProperties.getProperty(CONFIG_NAME))
            .setMonitorInterval(rootProperties.getProperty(MONITOR_INTERVAL, "0"))
            .setAdvertiser(rootProperties.getProperty(ADVERTISER_KEY));

        final Properties propertyPlaceholders = PropertiesUtil.extractSubset(rootProperties, "property");
        for (final String key : propertyPlaceholders.stringPropertyNames()) {
            builder.addProperty(key, propertyPlaceholders.getProperty(key));
        }

        final Map<String, Properties> scripts = PropertiesUtil.partitionOnCommonPrefixes(
            PropertiesUtil.extractSubset(rootProperties, "script"));
        for (final Map.Entry<String, Properties> entry : scripts.entrySet()) {
            final Properties scriptProps = entry.getValue();
            final String type = (String) scriptProps.remove("type");
            if (type == null) {
                throw new ConfigurationException("No type provided for script - must be Script or ScriptFile");
            }
            if (type.equalsIgnoreCase("script")) {
                builder.add(createScript(scriptProps));
            } else {
                builder.add(createScriptFile(scriptProps));
            }
        }

        final Properties levelProps = PropertiesUtil.extractSubset(rootProperties, "customLevel");
        if (levelProps.size() > 0) {
            for (final String key : levelProps.stringPropertyNames()) {
                builder.add(builder.newCustomLevel(key, Integer.parseInt(levelProps.getProperty(key))));
            }
        }

        final Map<String, Properties> filters = PropertiesUtil.partitionOnCommonPrefixes(
            PropertiesUtil.extractSubset(rootProperties, "filter"));
        for (final Map.Entry<String, Properties> entry : filters.entrySet()) {
            builder.add(createFilter(entry.getKey().trim(), entry.getValue()));
        }

        final Map<String, Properties> appenders = PropertiesUtil.partitionOnCommonPrefixes(
            PropertiesUtil.extractSubset(rootProperties, "appender"));
        for (final Map.Entry<String, Properties> entry : appenders.entrySet()) {
            builder.add(createAppender(entry.getKey().trim(), entry.getValue()));
        }

        final Map<String, Properties> loggers = PropertiesUtil.partitionOnCommonPrefixes(
            PropertiesUtil.extractSubset(rootProperties, "logger"));
        for (final Map.Entry<String, Properties> entry : loggers.entrySet()) {
            final String name = entry.getKey().trim();
            if (!name.equals(LoggerConfig.ROOT)) {
                builder.add(createLogger(name, entry.getValue()));
            }
        }

        final Properties props = PropertiesUtil.extractSubset(rootProperties, "rootLogger");
        if (props.size() > 0) {
            builder.add(createRootLogger(props));
        }

        return builder.build(false);
    }
