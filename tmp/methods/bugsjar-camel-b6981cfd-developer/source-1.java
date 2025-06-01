    protected Exchange onCompletion(final String key, final Exchange original, final Exchange aggregated, boolean fromTimeout) {
        // store the correlation key as property before we remove so the repository has that information
        if (original != null) {
            original.setProperty(Exchange.AGGREGATED_CORRELATION_KEY, key);
        }
        aggregated.setProperty(Exchange.AGGREGATED_CORRELATION_KEY, key);

        // remove from repository as its completed, we do this first as to trigger any OptimisticLockingException's
        aggregationRepository.remove(aggregated.getContext(), key, original);

        if (!fromTimeout && timeoutMap != null) {
            // cleanup timeout map if it was a incoming exchange which triggered the timeout (and not the timeout checker)
            timeoutMap.remove(key);
        }

        // this key has been closed so add it to the closed map
        if (closedCorrelationKeys != null) {
            closedCorrelationKeys.put(key, key);
        }

        if (fromTimeout) {
            // invoke timeout if its timeout aware aggregation strategy,
            // to allow any custom processing before discarding the exchange
            if (aggregationStrategy instanceof TimeoutAwareAggregationStrategy) {
                long timeout = getCompletionTimeout() > 0 ? getCompletionTimeout() : -1;
                ((TimeoutAwareAggregationStrategy) aggregationStrategy).timeout(aggregated, -1, -1, timeout);
            }
        }

        Exchange answer;
        if (fromTimeout && isDiscardOnCompletionTimeout()) {
            // discard due timeout
            LOG.debug("Aggregation for correlation key {} discarding aggregated exchange: {}", key, aggregated);
            // must confirm the discarded exchange
            aggregationRepository.confirm(aggregated.getContext(), aggregated.getExchangeId());
            // and remove redelivery state as well
            redeliveryState.remove(aggregated.getExchangeId());
            // the completion was from timeout and we should just discard it
            answer = null;
        } else {
            // the aggregated exchange should be published (sent out)
            answer = aggregated;
        }

        return answer;
    }
    Exchange get(CamelContext camelContext, String key);

    /**
     * Removes the exchange with the given correlation key, which should happen
     * when an {@link Exchange} is completed
    void remove(CamelContext camelContext, String key, Exchange exchange);

    /**
     * Confirms the completion of the {@link Exchange}.
