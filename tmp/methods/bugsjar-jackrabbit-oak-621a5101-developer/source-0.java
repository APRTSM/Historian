    public LoginContextProvider getLoginContextProvider(NodeStore nodeStore) {
        String appName = configuration.getConfigValue(PARAM_APP_NAME, DEFAULT_APP_NAME);
        Configuration loginConfig;
        try {
            loginConfig = Configuration.getConfiguration();
        } catch (SecurityException e) {
            log.warn("Failed to retrieve login configuration: using default.", e);
            loginConfig = new OakConfiguration();
            Configuration.setConfiguration(loginConfig);
        }
        return new LoginContextProviderImpl(appName, loginConfig, nodeStore, this);
    }
