    public synchronized void resumeRoute(String routeId) throws Exception {
        if (!routeSupportsSuspension(routeId)) {
            // start route if suspension is not supported
            startRoute(routeId);
            return;
        }

        RouteService routeService = routeServices.get(routeId);
        if (routeService != null) {
            resumeRouteService(routeService);
            // must resume the route as well
            Route route = getRoute(routeId);
            ServiceHelper.resumeService(route);
        }
    }
    public synchronized void suspendRoute(String routeId, long timeout, TimeUnit timeUnit) throws Exception {
        if (!routeSupportsSuspension(routeId)) {
            stopRoute(routeId, timeout, timeUnit);
            return;
        }

        RouteService routeService = routeServices.get(routeId);
        if (routeService != null) {
            List<RouteStartupOrder> routes = new ArrayList<RouteStartupOrder>(1);
            Route route = routeService.getRoutes().iterator().next();
            RouteStartupOrder order = new DefaultRouteStartupOrder(1, route, routeService);
            routes.add(order);

            getShutdownStrategy().suspend(this, routes, timeout, timeUnit);
            // must suspend route service as well
            suspendRouteService(routeService);
            // must suspend the route as well
            ServiceHelper.suspendService(route);
        }
    }
    public synchronized void suspendRoute(String routeId) throws Exception {
        if (!routeSupportsSuspension(routeId)) {
            // stop if we suspend is not supported
            stopRoute(routeId);
            return;
        }

        RouteService routeService = routeServices.get(routeId);
        if (routeService != null) {
            List<RouteStartupOrder> routes = new ArrayList<RouteStartupOrder>(1);
            Route route = routeService.getRoutes().iterator().next();
            RouteStartupOrder order = new DefaultRouteStartupOrder(1, route, routeService);
            routes.add(order);

            getShutdownStrategy().suspend(this, routes);
            // must suspend route service as well
            suspendRouteService(routeService);
            // must suspend the route as well
            ServiceHelper.suspendService(route);
        }
    }
    public static boolean suspendService(Object service) throws Exception {
        if (service instanceof SuspendableService) {
            SuspendableService ss = (SuspendableService) service;
            if (!ss.isSuspended()) {
                LOG.trace("Suspending service {}", service);
                ss.suspend();
                return true;
            } else {
                return false;
            }
        } else {
            stopService(service);
            return true;
        }
    }
    public static boolean resumeService(Object service) throws Exception {
        if (service instanceof SuspendableService) {
            SuspendableService ss = (SuspendableService) service;
            if (ss.isSuspended()) {
                LOG.debug("Resuming service {}", service);
                ss.resume();
                return true;
            } else {
                return false;
            }
        } else {
            startService(service);
            return true;
        }
    }
