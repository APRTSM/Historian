    protected ConfigurationSource getInputFromURI(final URI configLocation) {
        final File configFile = FileUtils.fileFromURI(configLocation);
        if (configFile != null && configFile.exists() && configFile.canRead()) {
            try {
                return new ConfigurationSource(new FileInputStream(configFile), configFile);
            } catch (final FileNotFoundException ex) {
                LOGGER.error("Cannot locate file " + configLocation.getPath(), ex);
            }
        }
        final String scheme = configLocation.getScheme();
        if (scheme == null || scheme.equals("classloader")) {
            final ClassLoader loader = this.getClass().getClassLoader();
            final ConfigurationSource source = getInputFromResource(configLocation.getPath(), loader);
            if (source != null) {
                return source;
            }
        }
        try {
            return new ConfigurationSource(configLocation.toURL().openStream(), configLocation.getPath());
        } catch (final MalformedURLException ex) {
            LOGGER.error("Invalid URL " + configLocation.toString(), ex);
        } catch (final IOException ex) {
            LOGGER.error("Unable to access " + configLocation.toString(), ex);
        } catch (final Exception ex) {
            LOGGER.error("Unable to access " + configLocation.toString(), ex);
        }
        return null;
    }
        public Configuration getConfiguration(final String name, final URI configLocation) {

            if (configLocation == null) {
                final String config = PropertiesUtil.getProperties().getStringProperty(CONFIGURATION_FILE_PROPERTY);
                if (config != null) {
                    final ClassLoader loader = this.getClass().getClassLoader();
                    final ConfigurationSource source = getInputFromString(config, loader);
                    if (source != null) {
                        for (final ConfigurationFactory factory : factories) {
                            final String[] types = factory.getSupportedTypes();
                            if (types != null) {
                                for (final String type : types) {
                                    if (type.equals("*") || config.endsWith(type)) {
                                        final Configuration c = factory.getConfiguration(source);
                                        if (c != null) {
                                            return c;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                for (final ConfigurationFactory factory : factories) {
                    final String[] types = factory.getSupportedTypes();
                    if (types != null) {
                        for (final String type : types) {
                            if (type.equals("*") || configLocation.getPath().endsWith(type)) {
                                final Configuration config = factory.getConfiguration(name, configLocation);
                                if (config != null) {
                                    return config;
                                }
                            }
                        }
                    }
                }
            }

            Configuration config = getConfiguration(true, name);
            if (config == null) {
                config = getConfiguration(true, null);
                if (config == null) {
                    config = getConfiguration(false, name);
                    if (config == null) {
                        config = getConfiguration(false, null);
                    }
                }
            }
            return config != null ? config : new DefaultConfiguration();
        }
