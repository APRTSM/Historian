        public Configuration getConfiguration(final String name, final URI configLocation) {

            if (configLocation == null) {
                final String config = this.substitutor.replace(
                    PropertiesUtil.getProperties().getStringProperty(CONFIGURATION_FILE_PROPERTY));
                if (config != null) {
                    ConfigurationSource source = null;
                    try {
                        source = getInputFromURI(new URI(config));
                    } catch (Exception ex) {
                        // Ignore the error and try as a String.
                    }
                    if (source == null) {
                        final ClassLoader loader = this.getClass().getClassLoader();
                        source = getInputFromString(config, loader);
                    }
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
                            if (type.equals("*") || configLocation.toString().endsWith(type)) {
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
