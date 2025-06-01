    protected void validate(String key, String value) {
        if (key.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Structured data keys are limited to 32 characters. key: " + key +
                " value: " + value);
        }
    }
