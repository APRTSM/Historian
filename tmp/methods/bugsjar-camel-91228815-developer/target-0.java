    public String toString() {
        String value = null;
        try {
            value = getEndpointUri();
        } catch (RuntimeException e) {
            // ignore any exception and use null for building the string value
        }
        return String.format("Endpoint[%s]", URISupport.sanitizeUri(value));
    }
