    private Class<?> loadClass(final ClassLoader lastLoader, final String className) {
        // XXX: this is overly complicated
        Class<?> clazz;
        if (lastLoader != null) {
            try {
                clazz = Loader.initializeClass(className, lastLoader);
                if (clazz != null) {
                    return clazz;
                }
            } catch (final Throwable ignore) {
                // Ignore exception.
            }
        }
        try {
            clazz = Loader.loadClass(className);
        } catch (final ClassNotFoundException ignored) {
            return initializeClass(className);
        } catch (final NoClassDefFoundError ignored) {
            return initializeClass(className);
        }
        return clazz;
    }
    private Class<?> initializeClass(final String className) {
        try {
            return Loader.initializeClass(className, this.getClass().getClassLoader());
        } catch (final ClassNotFoundException ignore) {
            return null;
        } catch (final NoClassDefFoundError ignore) {
            return null;
        }
    }
