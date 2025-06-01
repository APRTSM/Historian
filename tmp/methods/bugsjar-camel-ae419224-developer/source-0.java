    protected Schema createSchema() throws SAXException, IOException {
        SchemaFactory factory = getSchemaFactory();

        URL url = getSchemaUrl();
        if (url != null) {
            return factory.newSchema(url);
        }

        File file = getSchemaFile();
        if (file != null) {
            return factory.newSchema(file);
        }

        byte[] bytes = getSchemaAsByteArray();
        if (bytes != null) {
            return factory.newSchema(new StreamSource(new ByteArrayInputStream(schemaAsByteArray)));
        }

        Source source = getSchemaSource();
        return factory.newSchema(source);
    }
    public Schema getSchema() throws IOException, SAXException {
        if (schema == null) {
            schema = createSchema();
        }
        return schema;
    }
    public SchemaFactory getSchemaFactory() {
        if (schemaFactory == null) {
            schemaFactory = createSchemaFactory();
        }
        return schemaFactory;
    }
