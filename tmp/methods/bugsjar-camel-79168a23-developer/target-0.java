    protected synchronized void doStop() throws Exception {
        stopWatch.restart();
        log.info("Apache Camel " + getVersion() + " (CamelContext:" + getName() + ") is shutting down");
        EventHelper.notifyCamelContextStopping(this);

        // stop route inputs in the same order as they was started so we stop the very first inputs first
        try {
            shutdownStrategy.shutdown(this, getRouteStartupOrder());
        } catch (Throwable e) {
            log.warn("Error occurred while shutting down routes. This exception will be ignored.", e);
        }
        getRouteStartupOrder().clear();

        shutdownServices(routeServices.values());
        // do not clear route services or startup listeners as we can start Camel again and get the route back as before

        // but clear any suspend routes
        suspendedRouteServices.clear();

        // the stop order is important

        // shutdown debugger
        ServiceHelper.stopAndShutdownService(getDebugger());

        shutdownServices(endpoints.values());
        endpoints.clear();

        shutdownServices(components.values());
        components.clear();

        try {
            for (LifecycleStrategy strategy : lifecycleStrategies) {
                strategy.onContextStop(this);
            }
        } catch (Throwable e) {
            log.warn("Error occurred while stopping lifecycle strategies. This exception will be ignored.", e);
        }

        // shutdown services as late as possible
        shutdownServices(servicesToClose);
        servicesToClose.clear();

        // must notify that we are stopped before stopping the management strategy
        EventHelper.notifyCamelContextStopped(this);

        // stop the notifier service
        for (EventNotifier notifier : getManagementStrategy().getEventNotifiers()) {
            shutdownServices(notifier);
        }

        // shutdown management as the last one
        shutdownServices(managementStrategy);
        shutdownServices(lifecycleStrategies);
        lifecycleStrategies.clear();

        // stop the lazy created so they can be re-created on restart
        forceStopLazyInitialization();

        stopWatch.stop();
        if (log.isInfoEnabled()) {
            log.info("Uptime: " + getUptime());
            log.info("Apache Camel " + getVersion() + " (CamelContext: " + getName() + ") is shutdown in " + TimeUtils.printDuration(stopWatch.taken()));
        }

        // and clear start date
        startDate = null;
    }
    private void doStartCamel() throws Exception {
        if (isStreamCaching()) {
            // only add a new stream cache if not already configured
            if (StreamCaching.getStreamCaching(this) == null) {
                log.info("StreamCaching is enabled on CamelContext: " + getName());
                addInterceptStrategy(new StreamCaching());
            }
        }

        if (isTracing()) {
            // tracing is added in the DefaultChannel so we can enable it on the fly
            log.info("Tracing is enabled on CamelContext: " + getName());
        }

        if (isUseMDCLogging()) {
            // log if MDC has been enabled
            log.info("MDC logging is enabled on CamelContext: " + getName());
        }

        if (isHandleFault()) {
            // only add a new handle fault if not already configured
            if (HandleFault.getHandleFault(this) == null) {
                log.info("HandleFault is enabled on CamelContext: " + getName());
                addInterceptStrategy(new HandleFault());
            }
        }

        if (getDelayer() != null && getDelayer() > 0) {
            // only add a new delayer if not already configured
            if (Delayer.getDelayer(this) == null) {
                long millis = getDelayer();
                log.info("Delayer is enabled with: " + millis + " ms. on CamelContext: " + getName());
                addInterceptStrategy(new Delayer(millis));
            }
        }
        
        // register debugger
        if (getDebugger() != null) {
            log.info("Debugger: " + getDebugger() + " is enabled on CamelContext: " + getName());
            // register this camel context on the debugger
            getDebugger().setCamelContext(this);
            startService(getDebugger());
            addInterceptStrategy(new Debug(getDebugger()));
        }

        // start management strategy before lifecycles are started
        getManagementStrategy().start();

        // start lifecycle strategies
        ServiceHelper.startServices(lifecycleStrategies);
        Iterator<LifecycleStrategy> it = lifecycleStrategies.iterator();
        while (it.hasNext()) {
            LifecycleStrategy strategy = it.next();
            try {
                strategy.onContextStart(this);
            } catch (VetoCamelContextStartException e) {
                // okay we should not start Camel since it was vetoed
                log.warn("Lifecycle strategy vetoed starting CamelContext (" + getName() + ")", e);
                throw e;
            } catch (Exception e) {
                log.warn("Lifecycle strategy " + strategy + " failed starting CamelContext (" + getName() + ")", e);
                throw e;
            }
        }

        // start notifiers as services
        for (EventNotifier notifier : getManagementStrategy().getEventNotifiers()) {
            if (notifier instanceof Service) {
                Service service = (Service) notifier;
                for (LifecycleStrategy strategy : lifecycleStrategies) {
                    strategy.onServiceAdd(this, service, null);
                }
            }
            if (notifier instanceof Service) {
                startService((Service)notifier);
            }
        }

        // must let some bootstrap service be started before we can notify the starting event
        EventHelper.notifyCamelContextStarting(this);

        forceLazyInitialization();

        // re-create endpoint registry as the cache size limit may be set after the constructor of this instance was called.
        // and we needed to create endpoints up-front as it may be accessed before this context is started
        endpoints = new EndpointRegistry(this, endpoints);
        addService(endpoints);
        addService(executorServiceManager);
        addService(producerServicePool);
        addService(inflightRepository);
        addService(shutdownStrategy);
        addService(packageScanClassResolver);

        startServices(components.values());

        // start the route definitions before the routes is started
        startRouteDefinitions(routeDefinitions);

        // start routes
        if (doNotStartRoutesOnFirstStart) {
            log.info("Cannot start routes as CamelContext has been configured with autoStartup=false");
        }

        // invoke this logic to warmup the routes and if possible also start the routes
        doStartOrResumeRoutes(routeServices, true, !doNotStartRoutesOnFirstStart, false, true);

        // starting will continue in the start method
    }
