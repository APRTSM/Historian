    public boolean removeOnExceptionList(String id) {
        for (RouteContext routeContext : handlers.keySet()) {
            if (getRouteId(routeContext).equals(id)) {
                handlers.remove(routeContext);
                break;
            }
        }
        return super.removeOnExceptionList(id);
    }
    public boolean removeOnExceptionList(String id) {
        for (RouteContext routeContext : onExceptions.keySet()) {
            if (getRouteId(routeContext).equals(id)) {
                onExceptions.remove(routeContext);
                return true;
            }
        }
        return false;
    }
    protected String getRouteId(RouteContext routeContext) {
        CamelContext context = routeContext.getCamelContext();
        if (context != null) {
            return routeContext.getRoute().idOrCreate(context.getNodeIdFactory());
        } else {
            return routeContext.getRoute().getId();
        }
    }
    public synchronized boolean removeRoute(String routeId) throws Exception {
        // remove the route from ErrorHandlerBuilder if possible
        if (getErrorHandlerBuilder() instanceof ErrorHandlerBuilderSupport) {
            ErrorHandlerBuilderSupport builder = (ErrorHandlerBuilderSupport)getErrorHandlerBuilder();
            builder.removeOnExceptionList(routeId);
        }
        RouteService routeService = routeServices.get(routeId);
        if (routeService != null) {
            if (getRouteStatus(routeId).isStopped()) {
                routeService.setRemovingRoutes(true);
                shutdownRouteService(routeService);
                removeRouteDefinition(routeId);
                routeServices.remove(routeId);
                // remove route from startup order as well, as it was removed
                Iterator<RouteStartupOrder> it = routeStartupOrder.iterator();
                while (it.hasNext()) {
                    RouteStartupOrder order = it.next();
                    if (order.getRoute().getId().equals(routeId)) {
                        it.remove();
                    }
                }
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
