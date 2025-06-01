    ErrorHandlerBuilder cloneBuilder();
    public void addErrorHandlers(RouteContext routeContext, OnExceptionDefinition exception) {
        ErrorHandlerBuilder handler = handlers.get(routeContext);
        if (handler != null) {
            handler.addErrorHandlers(routeContext, exception);
        }
        super.addErrorHandlers(routeContext, exception);
    }
    public void setExceptionPolicyStrategy(ExceptionPolicyStrategy exceptionPolicyStrategy) {
        ObjectHelper.notNull(exceptionPolicyStrategy, "ExceptionPolicyStrategy");
        this.exceptionPolicyStrategy = exceptionPolicyStrategy;
    }
    public synchronized boolean removeRoute(String routeId) throws Exception {
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
