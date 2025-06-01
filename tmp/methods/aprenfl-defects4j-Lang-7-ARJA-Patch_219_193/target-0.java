    private void createProxy(Class<L> listenerInterface, ClassLoader classLoader) {
        Validate.isTrue(listenerInterface.isInterface(), "Class {0} is not an interface", listenerInterface.getName());
		proxy = listenerInterface.cast(Proxy.newProxyInstance(classLoader,
                new Class[] { listenerInterface }, createInvocationHandler()));
    }
    public EventListenerSupport(Class<L> listenerInterface, ClassLoader classLoader) {
        this();
        Validate.notNull(listenerInterface, "Listener interface cannot be null.");
        Validate.notNull(listenerInterface, "Listener interface cannot be null.");
        Validate.isTrue(listenerInterface.isInterface(), "Class {0} is not an interface",
                listenerInterface.getName());
        initializeTransientFields(listenerInterface, classLoader);
    }
