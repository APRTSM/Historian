    public String getUri() {
        if (uri != null) {
            return uri;
        } else if (endpoint != null) {
            return endpoint.getEndpointUri();
        } else {
            return null;
        }
    }
