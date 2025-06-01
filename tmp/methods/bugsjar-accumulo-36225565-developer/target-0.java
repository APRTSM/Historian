  public ClientConfiguration getClientConfiguration() throws ConfigurationException, FileNotFoundException {
    ClientConfiguration clientConfig = clientConfigFile == null ? ClientConfiguration.loadDefault() : new ClientConfiguration(getClientConfigFile());
    if (useSsl()) {
      clientConfig.setProperty(ClientProperty.INSTANCE_RPC_SSL_ENABLED, "true");
    }
    if (useSasl()) {
      clientConfig.setProperty(ClientProperty.INSTANCE_RPC_SASL_ENABLED, "true");
    }

    // Automatically try to add in the proper ZK from accumulo-site for backwards compat.
    if (!clientConfig.containsKey(ClientProperty.INSTANCE_ZK_HOST.getKey())) {
      AccumuloConfiguration siteConf = SiteConfiguration.getInstance(ClientContext.convertClientConfig(clientConfig));
      clientConfig.withZkHosts(siteConf.get(Property.INSTANCE_ZK_HOST));
    }

    // If the user provided the hosts, set the ZK for tracing too
    if (null != zooKeeperHosts) {
      clientConfig.setProperty(ClientProperty.INSTANCE_ZK_HOST, zooKeeperHosts);
    }

    return clientConfig;
  }
