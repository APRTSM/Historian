    public <T extends Endpoint> T getEndpoint(String name, Class<T> endpointType) {
        Endpoint endpoint = getEndpoint(name);
        if(endpoint == null){
		throw new IllegalArgumentException("No endpoint found with name: " + name);
        }
        if (endpoint instanceof InterceptSendToEndpoint) {
            endpoint = ((InterceptSendToEndpoint) endpoint).getDelegate();
        }
        if (endpointType.isInstance(endpoint)) {
            return endpointType.cast(endpoint);
        } else {
            throw new IllegalArgumentException("The endpoint is not of type: " + endpointType + " but is: "
                    + endpoint.getClass().getCanonicalName());
        }
    }
