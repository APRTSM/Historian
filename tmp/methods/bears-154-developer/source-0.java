  public static ConcurrentCompositeConfiguration createLocalConfig(List<ConfigModel> configModelList) {
    ConcurrentCompositeConfiguration config = new ConcurrentCompositeConfiguration();

    duplicateServiceCombConfigToCse(config,
        new ConcurrentMapConfiguration(new SystemConfiguration()),
        "configFromSystem");
    duplicateServiceCombConfigToCse(config,
        convertEnvVariable(new ConcurrentMapConfiguration(new EnvironmentConfiguration())),
        "configFromEnvironment");
    duplicateServiceCombConfigToCse(config,
        new DynamicConfiguration(
            new MicroserviceConfigurationSource(configModelList), new NeverStartPollingScheduler()),
        "configFromYamlFile");
    // If there is extra configurations, add it into config. Extra config has lowest priority.
    EXTRA_CONFIG_MAP.entrySet().stream()
        .filter(mapEntry -> !mapEntry.getValue().isEmpty())
        .forEachOrdered(configMapEntry ->
            duplicateServiceCombConfigToCse(config,
                new ConcurrentMapConfiguration(configMapEntry.getValue()),
                configMapEntry.getKey()));

    return config;
  }
