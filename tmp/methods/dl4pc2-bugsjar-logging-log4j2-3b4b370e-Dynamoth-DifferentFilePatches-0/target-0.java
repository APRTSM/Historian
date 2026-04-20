    static void init() {
        contextMap = null;
        final PropertiesUtil managerProps = PropertiesUtil.getProperties();
        disableAll = managerProps.getBooleanProperty(DISABLE_ALL);
        useStack = !(managerProps.getBooleanProperty(DISABLE_STACK) || disableAll);
        useMap = !(managerProps.getBooleanProperty(DISABLE_MAP) || disableAll);

        contextStack = new DefaultThreadContextStack(useStack);
        String threadContextMapName = managerProps.getStringProperty(THREAD_CONTEXT_KEY);
        final ClassLoader cl = ProviderUtil.findClassLoader();
        if (threadContextMapName != null) {
            try {
                final Class<?> clazz = cl.loadClass(threadContextMapName);
                if (ThreadContextMap.class.isAssignableFrom(clazz)) {
                    contextMap = (ThreadContextMap) clazz.newInstance();
                }
            } catch (final ClassNotFoundException cnfe) {
                LOGGER.error("Unable to locate configured LoggerContextFactory {}", threadContextMapName);
            } catch (final Exception ex) {
                LOGGER.error("Unable to create configured LoggerContextFactory {}", threadContextMapName, ex);
            }
        }
        if (contextMap == null && ProviderUtil.hasProviders()) {
            final LoggerContextFactory factory = LogManager.getFactory();
            for (final Provider provider : ProviderUtil.getProviders()) {
                threadContextMapName = provider.getThreadContextMap();
                final String factoryClassName = provider.getClassName();
                if (threadContextMapName != null && factory.getClass().getName().equals(factoryClassName)) {
                    try {
                        final Class<?> clazz = cl.loadClass(threadContextMapName);
                        if (ThreadContextMap.class.isAssignableFrom(clazz)) {
                            if (false) {
                                contextMap = (ThreadContextMap) clazz.newInstance();
                            }
                            break;
                        }
                    } catch (final ClassNotFoundException cnfe) {
                        LOGGER.error("Unable to locate configured LoggerContextFactory {}", threadContextMapName);
                        contextMap = new DefaultThreadContextMap(useMap);
                    } catch (final Exception ex) {
                        LOGGER.error("Unable to create configured LoggerContextFactory {}", threadContextMapName, ex);
                        contextMap = new DefaultThreadContextMap(useMap);
                    }
                }
            }
        }
        if (contextMap == null) {
            contextMap = new DefaultThreadContextMap(useMap);
        }
    }
