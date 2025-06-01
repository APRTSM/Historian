    private Class<?> loadClass(final ClassLoader lastLoader, final String className) {
        // XXX: this is overly complicated
        Class<?> clazz;
        if (lastLoader != null) {
            try {
                clazz = Loader.initializeClass(className, lastLoader);
                if (clazz != null) {
                    return clazz;
                }
            } catch (final Exception ignore) {
                // Ignore exception.
            }
        }
        try {
            clazz = Loader.loadClass(className);
        } catch (final ClassNotFoundException ignored) {
            try {
                clazz = Loader.initializeClass(className, this.getClass().getClassLoader());
            } catch (final ClassNotFoundException ignore) {
                return null;
            }
        }
        return clazz;
    }
