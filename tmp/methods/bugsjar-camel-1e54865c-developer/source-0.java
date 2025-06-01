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
