    public NotifyBuilder fromRoute(final String routeId) {
        stack.add(new EventPredicateSupport() {

            @Override
            public boolean isAbstract() {
                // is abstract as its a filter
                return true;
            }

            @Override
            public boolean onExchange(Exchange exchange) {
                String id = EndpointHelper.getRouteIdFromEndpoint(exchange.getFromEndpoint());

                if (id == null) {
                    id = exchange.getFromRouteId();
                }

                // filter non matching exchanges
                return EndpointHelper.matchPattern(id, routeId);
            }

            public boolean matches() {
                // should be true as we use the onExchange to filter
                return true;
            }

            @Override
            public String toString() {
                return "fromRoute(" + routeId + ")";
            }
        });
        return this;
    }
