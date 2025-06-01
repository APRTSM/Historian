    public void processSwagger(Swagger swagger) {
        try {
            final ObjectMapper mapper = new ObjectMapper(new YAMLFactory().configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true));
            configureMapper(mapper);
            String swaggerString = mapper.writeValueAsString(swagger);
            String outputFile = outputFolder + File.separator + this.outputFile;
            FileUtils.writeStringToFile(new File(outputFile), swaggerString);
            LOGGER.debug("wrote file to " + outputFile);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
