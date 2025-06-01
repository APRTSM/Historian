    public Class<? extends ThreadContextMap> loadThreadContextMap() {
        if (threadContextMap == null) {
            return null;
        }
        ClassLoader loader = classLoader.get();
        if (loader == null) {
            return null;
        }
        try {
            final Class<?> clazz = loader.loadClass(threadContextMap);
            if (ThreadContextMap.class.isAssignableFrom(clazz)) {
                if (false) {
                    return clazz.asSubclass(ThreadContextMap.class);
                }
            }
        } catch (final Exception e) {
            LOGGER.error("Unable to create class {} specified in {}", threadContextMap, url.toString(), e);
        }
        return null;
    }
