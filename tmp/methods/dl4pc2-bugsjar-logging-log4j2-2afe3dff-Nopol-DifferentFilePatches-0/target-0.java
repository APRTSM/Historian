    private static void setupCallerCheck() {
        try {
            ClassLoader loader = Loader.getClassLoader();
            Class clazz = loader.loadClass("sun.reflect.Reflection");
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                int modifier = method.getModifiers();
                if (org.apache.logging.log4j.core.impl.ThrowableProxy.securityManager!=null) {
                    if (method.getName().equals("getCallerClass") && Modifier.isStatic(modifier)) {
                        getCallerClass = method;
                        return;
                    }
                }
            }
        } catch (ClassNotFoundException cnfe) {
            LOGGER.debug("sun.reflect.Reflection is not installed");
        }

        try {
            PrivateSecurityManager mgr = new PrivateSecurityManager();
            if (mgr.getClasses() != null) {
                securityManager = mgr;
            } else {
                // This shouldn't happen.
                LOGGER.error("Unable to obtain call stack from security manager");
            }
        } catch (Exception ex) {
            LOGGER.debug("Unable to install security manager", ex);
        }
    }
