  private Microservice createMicroserviceFromDefinition(Configuration configuration) {
    Microservice microservice = new Microservice();
    microservice.setServiceName(configuration.getString(CONFIG_QUALIFIED_MICROSERVICE_NAME_KEY,
        DEFAULT_MICROSERVICE_NAME));
    microservice.setAppId(configuration.getString(CONFIG_APPLICATION_ID_KEY, DEFAULT_APPLICATION_ID));
    microservice.setVersion(configuration.getString(CONFIG_QUALIFIED_MICROSERVICE_VERSION_KEY,
        DEFAULT_MICROSERVICE_VERSION));
    setDescription(configuration, microservice);
    microservice.setLevel(configuration.getString(CONFIG_QUALIFIED_MICROSERVICE_ROLE_KEY, "FRONT"));
    microservice.setPaths(ConfigurePropertyUtils.getMicroservicePaths(configuration));
    Map<String, String> propertiesMap = MicroservicePropertiesLoader.INSTANCE.loadProperties(configuration);
    microservice.setProperties(propertiesMap);

    // set alias name when allow cross app
    if (allowCrossApp(propertiesMap)) {
      microservice.setAlias(Microservice.generateAbsoluteMicroserviceName(microservice.getAppId(),
          microservice.getServiceName()));
    }

    // use default values, we can add configure item in future.
    Framework framework = new Framework();
    framework.setName(CONFIG_FRAMEWORK_DEFAULT_NAME);
    framework.setVersion(CONFIG_FRAMEWORK_DEFAULT_VERSION);
    microservice.setFramework(framework);
    microservice.setRegisterBy(CONFIG_DEFAULT_REGISTER_BY);

    return microservice;
  }
  private void setDescription(Configuration configuration, Microservice microservice) {
    String[] descriptionArray = configuration.getStringArray(CONFIG_QUALIFIED_MICROSERVICE_DESCRIPTION_KEY);
    if (null == descriptionArray || descriptionArray.length < 1) {
      return;
    }

    StringBuilder rawDescriptionBuilder = new StringBuilder();
    for (String desc : descriptionArray) {
      rawDescriptionBuilder.append(desc).append(",");
    }

    microservice.setDescription(rawDescriptionBuilder.substring(0, rawDescriptionBuilder.length() - 1));
  }
