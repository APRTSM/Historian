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
