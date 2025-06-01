    public void onStartup(final Set<Class<?>> classes, final ServletContext servletContext) throws ServletException {
        servletContext.log("Log4jServletContainerInitializer starting up Log4j in Servlet 3.0+ environment.");

        final Log4jWebInitializer initializer = Log4jWebInitializerImpl.getLog4jWebInitializer(servletContext);
        initializer.initialize();
        initializer.setLoggerContext(); // the application is just now starting to start up

        servletContext.addListener(new Log4jServletContextListener());

        final FilterRegistration.Dynamic filter = servletContext.addFilter("log4jServletFilter", new Log4jServletFilter());
        filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");
    }
