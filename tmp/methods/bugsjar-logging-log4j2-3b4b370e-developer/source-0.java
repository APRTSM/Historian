    public Configuration reconfigure() {
        if (configFile != null) {
            try {
                final ConfigurationFactory.ConfigurationSource source =
                    new ConfigurationFactory.ConfigurationSource(new FileInputStream(configFile), configFile);
                final XmlConfiguration config = new XmlConfiguration(source);
                if (config.rootElement == null) {
                    return null;
                }
            } catch (final FileNotFoundException ex) {
                LOGGER.error("Cannot locate file " + configFile, ex);
            }
        }
        return null;
    }
