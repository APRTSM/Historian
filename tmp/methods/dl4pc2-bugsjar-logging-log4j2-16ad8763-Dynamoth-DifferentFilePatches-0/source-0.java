    public Class<? extends ThreadContextMap> loadThreadContextMap() {
        if (threadContextMap == null) {
            return null;
        }
        try {
            final Class<?> clazz = classLoader.loadClass(threadContextMap);
            if (ThreadContextMap.class.isAssignableFrom(clazz)) {
                return clazz.asSubclass(ThreadContextMap.class);
            }
        } catch (final Exception e) {
            LOGGER.error("Unable to create class {} specified in {}", threadContextMap, url.toString(), e);
        }
        return null;
    }
