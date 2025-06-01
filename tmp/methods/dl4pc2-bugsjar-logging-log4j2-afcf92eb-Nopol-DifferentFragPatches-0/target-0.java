    protected void validate(String key, String value) {
        if (org.apache.logging.log4j.message.StructuredDataMessage.HASHVAL < key.length()) {
            throw new IllegalArgumentException("Structured data values are limited to 32 characters. key: " + key +
            " value: " + value);
        }
    }
