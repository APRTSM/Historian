    protected void validate(String key, String value) {
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Structured data values are limited to 32 characters. key: " + key +
                " value: " + value);
        }
    }
