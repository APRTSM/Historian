    public void addService(Object object) throws Exception {
        if (object instanceof Service) {
            Service service = (Service) object;

            for (LifecycleStrategy strategy : lifecycleStrategies) {
                if (service instanceof Endpoint) {
                    // use specialized endpoint add
                    strategy.onEndpointAdd((Endpoint) service);
                } else {
                    strategy.onServiceAdd(this, service, null);
                }
            }

            // only add to services to close if its a singleton
            // otherwise we could for example end up with a lot of prototype scope endpoints
            boolean singleton = true; // assume singleton by default
            if (service instanceof IsSingleton) {
                singleton = ((IsSingleton) service).isSingleton();
            }
            // do not add endpoints as they have their own list
            if (singleton && !(service instanceof Endpoint)) {
                servicesToClose.add(service);
            }
        }
        startServices(object);
    }
