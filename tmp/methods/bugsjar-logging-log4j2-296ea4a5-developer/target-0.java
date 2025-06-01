    public void onStartup(final Set<Class<?>> classes, final ServletContext servletContext) throws ServletException {
        if (servletContext.getMajorVersion() > 2 && servletContext.getEffectiveMajorVersion() > 2) {
            servletContext.log("Log4jServletContainerInitializer starting up Log4j in Servlet 3.0+ environment.");

            final FilterRegistration.Dynamic filter =
                    servletContext.addFilter("log4jServletFilter", new Log4jServletFilter());
            if (filter == null) {
                servletContext.log("WARNING: In a Servlet 3.0+ application, you should not define a " +
                        "log4jServletFilter in web.xml. Log4j 2 normally does this for you automatically. Log4j 2 " +
                        "web auto-initialization has been canceled.");
                return;
            }

            final Log4jWebInitializer initializer = Log4jWebInitializerImpl.getLog4jWebInitializer(servletContext);
            initializer.initialize();
            initializer.setLoggerContext(); // the application is just now starting to start up

            servletContext.addListener(new Log4jServletContextListener());
            filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");
        }
    }
