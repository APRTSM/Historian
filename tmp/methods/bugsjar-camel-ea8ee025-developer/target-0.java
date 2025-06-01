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
