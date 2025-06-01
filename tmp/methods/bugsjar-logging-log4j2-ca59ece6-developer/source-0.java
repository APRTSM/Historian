    private LoggerContext locateContext(final ClassLoader loader, final URI configLocation) {
        final String name = loader.toString();
        AtomicReference<WeakReference<LoggerContext>> ref = CONTEXT_MAP.get(name);
        if (ref == null) {
            if (configLocation == null) {
                ClassLoader parent = loader.getParent();
                while (parent != null) {

                    ref = CONTEXT_MAP.get(parent.toString());
                    if (ref != null) {
                        final WeakReference<LoggerContext> r = ref.get();
                        final LoggerContext ctx = r.get();
                        if (ctx != null) {
                            return ctx;
                        }
                    }
                    parent = parent.getParent();
                    /*  In Tomcat 6 the parent of the JSP classloader is the webapp classloader which would be
                    configured by the WebAppContextListener. The WebAppClassLoader is also the ThreadContextClassLoader.
                    In JBoss 5 the parent of the JSP ClassLoader is the WebAppClassLoader which is also the
                    ThreadContextClassLoader. However, the parent of the WebAppClassLoader is the ClassLoader
                    that is configured by the WebAppContextListener.

                    ClassLoader threadLoader = null;
                    try {
                        threadLoader = Thread.currentThread().getContextClassLoader();
                    } catch (Exception ex) {
                        // Ignore SecurityException
                    }
                    if (threadLoader != null && threadLoader == parent) {
                        break;
                    } else {
                        parent = parent.getParent();
                    } */
                }
            }
            LoggerContext ctx = new LoggerContext(name, null, configLocation);
            final AtomicReference<WeakReference<LoggerContext>> r =
                new AtomicReference<WeakReference<LoggerContext>>();
            r.set(new WeakReference<LoggerContext>(ctx));
            CONTEXT_MAP.putIfAbsent(loader.toString(), r);
            ctx = CONTEXT_MAP.get(name).get().get();
            return ctx;
        }
        final WeakReference<LoggerContext> r = ref.get();
        LoggerContext ctx = r.get();
        if (ctx != null) {
            return ctx;
        }
        ctx = new LoggerContext(name, null, configLocation);
        ref.compareAndSet(r, new WeakReference<LoggerContext>(ctx));
        return ctx;
    }
