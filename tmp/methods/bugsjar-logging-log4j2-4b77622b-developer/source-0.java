    public XmlConfiguration(final ConfigurationSource configSource) {
        super(configSource);
        final File configFile = configSource.getFile();
        byte[] buffer = null;

        try {
            final InputStream configStream = configSource.getInputStream();
            try {
                buffer = toByteArray(configStream);
            } finally {
                configStream.close();
            }
            final InputSource source = new InputSource(new ByteArrayInputStream(buffer));
            final Document document = newDocumentBuilder().parse(source);
            rootElement = document.getDocumentElement();
            final Map<String, String> attrs = processAttributes(rootNode, rootElement);
            final StatusConfiguration statusConfig = new StatusConfiguration().withVerboseClasses(VERBOSE_CLASSES)
                    .withStatus(getDefaultStatus());
            for (final Map.Entry<String, String> entry : attrs.entrySet()) {
                final String key = entry.getKey();
                final String value = getStrSubstitutor().replace(entry.getValue());
                if ("status".equalsIgnoreCase(key)) {
                    statusConfig.withStatus(value);
                } else if ("dest".equalsIgnoreCase(key)) {
                    statusConfig.withDestination(value);
                } else if ("shutdownHook".equalsIgnoreCase(key)) {
                    isShutdownHookEnabled = !"disable".equalsIgnoreCase(value);
                } else if ("verbose".equalsIgnoreCase(key)) {
                    statusConfig.withVerbosity(value);
                } else if ("packages".equalsIgnoreCase(key)) {
                    final String[] packages = value.split(Patterns.COMMA_SEPARATOR);
                    for (final String p : packages) {
                        PluginManager.addPackage(p);
                    }
                } else if ("name".equalsIgnoreCase(key)) {
                    setName(value);
                } else if ("strict".equalsIgnoreCase(key)) {
                    strict = Boolean.parseBoolean(value);
                } else if ("schema".equalsIgnoreCase(key)) {
                    schema = value;
                } else if ("monitorInterval".equalsIgnoreCase(key)) {
                    final int interval = Integer.parseInt(value);
                    if (interval > 0 && configFile != null) {
                        monitor = new FileConfigurationMonitor(this, configFile, listeners, interval);
                    }
                } else if ("advertiser".equalsIgnoreCase(key)) {
                    createAdvertiser(value, configSource, buffer, "text/xml");
                }
            }
            statusConfig.initialize();
        } catch (final SAXException domEx) {
            LOGGER.error("Error parsing " + configSource.getLocation(), domEx);
        } catch (final IOException ioe) {
            LOGGER.error("Error parsing " + configSource.getLocation(), ioe);
        } catch (final ParserConfigurationException pex) {
            LOGGER.error("Error parsing " + configSource.getLocation(), pex);
        }
        if (strict && schema != null && buffer != null) {
            InputStream is = null;
            try {
                is = Loader.getResourceAsStream(schema, XmlConfiguration.class.getClassLoader());
            } catch (final Exception ex) {
                LOGGER.error("Unable to access schema {}", this.schema, ex);
            }
            if (is != null) {
                final Source src = new StreamSource(is, LOG4J_XSD);
                final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = null;
                try {
                    schema = factory.newSchema(src);
                } catch (final SAXException ex) {
                    LOGGER.error("Error parsing Log4j schema", ex);
                }
                if (schema != null) {
                    final Validator validator = schema.newValidator();
                    try {
                        validator.validate(new StreamSource(new ByteArrayInputStream(buffer)));
                    } catch (final IOException ioe) {
                        LOGGER.error("Error reading configuration for validation", ioe);
                    } catch (final SAXException ex) {
                        LOGGER.error("Error validating configuration", ex);
                    }
                }
            }
        }

        if (getName() == null) {
            setName(configSource.getLocation());
        }
    }
