    public void addExceptionPolicy(OnExceptionDefinition exceptionType) {
        Processor processor = exceptionType.getErrorHandler();
        addChildService(processor);

        List<Class> list = exceptionType.getExceptionClasses();

        for (Class clazz : list) {
            RouteDefinition route = ProcessorDefinitionHelper.getRoute(exceptionType);
            String routeId = route != null ? route.getId() : null;
            ExceptionPolicyKey key = new ExceptionPolicyKey(routeId, clazz, exceptionType.getOnWhen());
            exceptionPolicies.put(key, exceptionType);
        }
    }
    public OnExceptionDefinition getExceptionPolicy(Map<ExceptionPolicyKey, OnExceptionDefinition> exceptionPolicies,
                                                    Exchange exchange, Throwable exception) {

        Map<Integer, OnExceptionDefinition> candidates = new TreeMap<Integer, OnExceptionDefinition>();
        Map<ExceptionPolicyKey, OnExceptionDefinition> routeScoped = new LinkedHashMap<ExceptionPolicyKey, OnExceptionDefinition>();
        Map<ExceptionPolicyKey, OnExceptionDefinition> contextScoped = new LinkedHashMap<ExceptionPolicyKey, OnExceptionDefinition>();

        // split policies into route and context scoped
        initRouteAndContextScopedExceptionPolicies(exceptionPolicies, routeScoped, contextScoped);

        // at first check route scoped as we prefer them over context scoped
        // recursive up the tree using the iterator
        boolean exactMatch = false;
        Iterator<Throwable> it = createExceptionIterator(exception);
        while (!exactMatch && it.hasNext()) {
            // we should stop looking if we have found an exact match
            exactMatch = findMatchedExceptionPolicy(routeScoped, exchange, it.next(), candidates);
        }

        // fallback to check context scoped (only do this if there was no exact match)
        it = createExceptionIterator(exception);
        while (!exactMatch && it.hasNext()) {
            // we should stop looking if we have found an exact match
            exactMatch = findMatchedExceptionPolicy(contextScoped, exchange, it.next(), candidates);
        }

        // now go through the candidates and find the best
        if (LOG.isTraceEnabled()) {
            LOG.trace("Found " + candidates.size() + " candidates");
        }

        if (candidates.isEmpty()) {
            // no type found
            return null;
        } else {
            // return the first in the map as its sorted and we checked route scoped first, which we prefer
            return candidates.values().iterator().next();
        }
    }
    private void initRouteAndContextScopedExceptionPolicies(Map<ExceptionPolicyKey, OnExceptionDefinition> exceptionPolicies,
                                                            Map<ExceptionPolicyKey, OnExceptionDefinition> routeScoped,
                                                            Map<ExceptionPolicyKey, OnExceptionDefinition> contextScoped) {

        // loop through all the entries and split into route and context scoped
        Set<Map.Entry<ExceptionPolicyKey, OnExceptionDefinition>> entries = exceptionPolicies.entrySet();
        for (Map.Entry<ExceptionPolicyKey, OnExceptionDefinition> entry : entries) {
            if (entry.getKey().getRouteId() != null) {
                routeScoped.put(entry.getKey(), entry.getValue());
            } else {
                contextScoped.put(entry.getKey(), entry.getValue());
            }
        }
    }
