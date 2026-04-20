    public void stop() {
        this.setStopping();
        LOGGER.trace("Stopping {}...", this);

        // LOG4J2-392 first stop AsyncLogger Disruptor thread
        final LoggerContextFactory factory = LogManager.getFactory();
        if (factory instanceof Log4jContextFactory) {
            ContextSelector selector = ((Log4jContextFactory) factory).getSelector();
            if (selector instanceof AsyncLoggerContextSelector) { // all loggers are async
                // TODO until LOG4J2-493 is fixed we can only stop AsyncLogger once!
                // but LoggerContext.setConfiguration will call config.stop()
                // every time the configuration changes...
                //
                // Uncomment the line below after LOG4J2-493 is fixed
                //AsyncLogger.stop();
                //LOGGER.trace("AbstractConfiguration stopped AsyncLogger disruptor.");
            }
        }
        // similarly, first stop AsyncLoggerConfig Disruptor thread(s)
        final Set<LoggerConfig> alreadyStopped = new HashSet<LoggerConfig>();
        int asyncLoggerConfigCount = 0;
        for (final LoggerConfig logger : loggers.values()) {
            if (logger instanceof AsyncLoggerConfig) {
                // LOG4J2-520, LOG4J2-392:
                // Important: do not clear appenders until after all AsyncLoggerConfigs
                // have been stopped! Stopping the last AsyncLoggerConfig will
                // shut down the disruptor and wait for all enqueued events to be processed.
                // Only *after this* the appenders can be cleared or events will be lost.
                logger.stop();
                asyncLoggerConfigCount++;
                alreadyStopped.add(logger);
            }
        }
        if (root instanceof AsyncLoggerConfig) {
            root.stop();
            asyncLoggerConfigCount++;
            alreadyStopped.add(root);
        }
        LOGGER.trace("AbstractConfiguration stopped {} AsyncLoggerConfigs.", asyncLoggerConfigCount);

        // Stop the appenders in reverse order in case they still have activity.
        final Appender[] array = appenders.values().toArray(new Appender[appenders.size()]);

        // LOG4J2-511, LOG4J2-392 stop AsyncAppenders first
        int asyncAppenderCount = 0;
        for (int i = array.length - 1; i >= 0; --i) {
            if (array[i] instanceof AsyncAppender) {
                array[i].stop();
                asyncAppenderCount++;
            }
        }
        LOGGER.trace("AbstractConfiguration stopped {} AsyncAppenders.", asyncAppenderCount);

        int appenderCount = 0;
        for (int i = array.length - 1; i >= 0; --i) {
            if (array[i].isStarted()) { // then stop remaining Appenders
                array[i].stop();
                appenderCount++;
            }
        }
        LOGGER.trace("AbstractConfiguration stopped {} Appenders.", appenderCount);

        int loggerCount = 0;
        for (final LoggerConfig logger : loggers.values()) {
            // clear appenders, even if this logger is already stopped.
            logger.clearAppenders();
            
            // AsyncLoggerConfigHelper decreases its ref count when an AsyncLoggerConfig is stopped.
            // Stopping the same AsyncLoggerConfig twice results in an incorrect ref count and
            // the shared Disruptor may be shut down prematurely, resulting in NPE or other errors.
            if (alreadyStopped.contains(logger)) {
                continue;
            }
            logger.stop();
            loggerCount++;
        }
        LOGGER.trace("AbstractConfiguration stopped {} Loggers.", loggerCount);

        // AsyncLoggerConfigHelper decreases its ref count when an AsyncLoggerConfig is stopped.
        // Stopping the same AsyncLoggerConfig twice results in an incorrect ref count and
        // the shared Disruptor may be shut down prematurely, resulting in NPE or other errors.
        if (!alreadyStopped.contains(root)) {
            root.stop();
        }
        super.stop();
        if (advertiser != null && advertisement != null) {
            advertiser.unadvertise(advertisement);
        }
        LOGGER.debug("Stopped {} OK", this);
    }
    public void start() {
        LOGGER.debug("Starting configuration {}", this);
        this.setStarting();
        pluginManager.collectPlugins();
        final PluginManager levelPlugins = new PluginManager("Level");
        levelPlugins.collectPlugins();
        final Map<String, PluginType<?>> plugins = levelPlugins.getPlugins();
        if (plugins != null) {
            for (final PluginType<?> type : plugins.values()) {
                try {
                    // Cause the class to be initialized if it isn't already.
                    Loader.initializeClass(type.getPluginClass().getName(), type.getPluginClass().getClassLoader());
                } catch (final Exception ex) {
                    LOGGER.error("Unable to initialize {} due to {}: {}", type.getPluginClass().getName(),
                            ex.getClass().getSimpleName(), ex.getMessage());
                }
            }
        }
        setup();
        setupAdvertisement();
        doConfigure();
        final Set<LoggerConfig> alreadyStarted = new HashSet<LoggerConfig>();
        for (final LoggerConfig logger : loggers.values()) {
            logger.start();
            alreadyStarted.add(logger);
        }
        for (final Appender appender : appenders.values()) {
            appender.start();
        }
        if (!alreadyStarted.contains(root)) { // LOG4J2-392
            root.start(); // LOG4J2-336
        }
        super.start();
        LOGGER.debug("Started configuration {} OK.", this);
    }
