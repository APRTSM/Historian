    public SchemaFactory getSchemaFactory() {
        if (schemaFactory == null) {
            synchronized (this) {
                if (schemaFactory == null) {
                    schemaFactory = createSchemaFactory();
                }
            }
        }
        return schemaFactory;
    }
    public Schema getSchema() throws IOException, SAXException {
        if (schema == null) {
            synchronized (this) {
                if (schema == null) {
                    schema = createSchema();
                }
            }
        }
        return schema;
    }
    protected Schema createSchema() throws SAXException, IOException {
        SchemaFactory factory = getSchemaFactory();

        URL url = getSchemaUrl();
        if (url != null) {
            synchronized (this) {
                return factory.newSchema(url);
            }
        }

        File file = getSchemaFile();
        if (file != null) {
            synchronized (this) {
                return factory.newSchema(file);
            }
        }

        byte[] bytes = getSchemaAsByteArray();
        if (bytes != null) {
            synchronized (this) {
                return factory.newSchema(new StreamSource(new ByteArrayInputStream(schemaAsByteArray)));
            }
        }

        Source source = getSchemaSource();
        synchronized (this) {
            return factory.newSchema(source);
        }
    }
