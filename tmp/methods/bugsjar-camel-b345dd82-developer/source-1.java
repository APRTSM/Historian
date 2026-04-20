    public void addExceptionPolicy(OnExceptionDefinition exceptionType) {
        Processor processor = exceptionType.getErrorHandler();
        addChildService(processor);

        List<Class> list = exceptionType.getExceptionClasses();

        for (Class clazz : list) {
            ExceptionPolicyKey key = new ExceptionPolicyKey(clazz, exceptionType.getOnWhen());
            exceptionPolicies.put(key, exceptionType);
        }
    }
    public OnExceptionDefinition getExceptionPolicy(Map<ExceptionPolicyKey, OnExceptionDefinition> exceptionPolicies,
                                                    Exchange exchange, Throwable exception) {

        Map<Integer, OnExceptionDefinition> candidates = new TreeMap<Integer, OnExceptionDefinition>();

        // recursive up the tree using the iterator
        boolean exactMatch = false;
        Iterator<Throwable> it = createExceptionIterator(exception);
        while (!exactMatch && it.hasNext()) {
            // we should stop looking if we have found an exact match
            exactMatch = findMatchedExceptionPolicy(exceptionPolicies, exchange, it.next(), candidates);
        }

        // now go through the candidates and find the best

        if (LOG.isTraceEnabled()) {
            LOG.trace("Found " + candidates.size() + " candidates");
        }

        if (candidates.isEmpty()) {
            // no type found
            return null;
        } else {
            // return the first in the map as its sorted and
            return candidates.values().iterator().next();
        }
    }
