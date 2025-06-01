    public boolean removeOnExceptionList(String id) {
        for (RouteContext routeContext : handlers.keySet()) {
            if (getRouteId(routeContext).equals(id)) {
                handlers.remove(routeContext);
                break;
            }
        }
        return super.removeOnExceptionList(id);
    }
