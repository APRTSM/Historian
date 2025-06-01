    public void unregister(ObjectName name) throws JMException {
        if (isRegistered(name)) {
            server.unregisterMBean(mbeansRegistered.get(name));
            LOG.debug("Unregistered MBean with ObjectName: {}", name);
        }
        mbeansRegistered.remove(name);
    }
    public boolean isRegistered(ObjectName name) {
        return (mbeansRegistered.containsKey(name)
                && server.isRegistered(mbeansRegistered.get(name)))
                || server.isRegistered(name);
    }
    private void registerMBeanWithServer(Object obj, ObjectName name, boolean forceRegistration)
        throws JMException {

        // have we already registered the bean, there can be shared instances in the camel routes
        boolean exists = isRegistered(name);
        if (exists) {
            if (forceRegistration) {
                LOG.info("ForceRegistration enabled, unregistering existing MBean with ObjectName: {}", name);
                server.unregisterMBean(name);
            } else {
                // okay ignore we do not want to force it and it could be a shared instance
                LOG.debug("MBean already registered with ObjectName: {}", name);
            }
        }

        // register bean if by force or not exists
        ObjectInstance instance = null;
        if (forceRegistration || !exists) {
            LOG.trace("Registering MBean with ObjectName: {}", name);
            instance = server.registerMBean(obj, name);
        }

        // need to use the name returned from the server as some JEE servers may modify the name
        if (instance != null) {
            ObjectName registeredName = instance.getObjectName();
            LOG.debug("Registered MBean with ObjectName: {}", registeredName);
            mbeansRegistered.put(name, registeredName);
        }
    }
    protected void doStop() throws Exception {
        // close JMX Connector
        if (cs != null) {
            try {
                cs.stop();
            } catch (IOException e) {
                LOG.debug("Error occurred during stopping JMXConnectorService: "
                        + cs + ". This exception will be ignored.");
            }
            cs = null;
        }

        if (mbeansRegistered.isEmpty()) {
            return;
        }

        // Using the array to hold the busMBeans to avoid the CurrentModificationException
        ObjectName[] mBeans = mbeansRegistered.keySet().toArray(new ObjectName[mbeansRegistered.size()]);
        int caught = 0;
        for (ObjectName name : mBeans) {
            try {
                unregister(name);
            } catch (Exception e) {
                LOG.info("Exception unregistering MBean with name " + name, e);
                caught++;
            }
        }
        if (caught > 0) {
            LOG.warn("A number of " + caught
                     + " exceptions caught while unregistering MBeans during stop operation."
                     + " See INFO log for details.");
        }
    }
