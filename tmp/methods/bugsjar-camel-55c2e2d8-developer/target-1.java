    public synchronized BlockingQueue<Exchange> getOrCreateQueue(String uri, Integer size) {
        String key = getQueueKey(uri);

        QueueReference ref = getQueues().get(key);
        if (ref != null) {
            // add the reference before returning queue
            ref.addReference();
            return ref.getQueue();
        }

        // create queue
        BlockingQueue<Exchange> queue;
        if (size != null && size > 0) {
            queue = new LinkedBlockingQueue<Exchange>(size);
        } else {
            if (getQueueSize() > 0) {
                queue = new LinkedBlockingQueue<Exchange>(getQueueSize());
            } else {
                queue = new LinkedBlockingQueue<Exchange>();
            }
        }

        // create and add a new reference queue
        ref = new QueueReference(queue);
        ref.addReference();
        getQueues().put(key, ref);

        return queue;
    }
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        int consumers = getAndRemoveParameter(parameters, "concurrentConsumers", Integer.class, defaultConcurrentConsumers);
        boolean limitConcurrentConsumers = getAndRemoveParameter(parameters, "limitConcurrentConsumers", Boolean.class, true);
        if (limitConcurrentConsumers && consumers >  maxConcurrentConsumers) {
            throw new IllegalArgumentException("The limitConcurrentConsumers flag in set to true. ConcurrentConsumers cannot be set at a value greater than "
                    + maxConcurrentConsumers + " was " + consumers);
        }
        Integer size = getAndRemoveParameter(parameters, "size", Integer.class);
        SedaEndpoint answer = new SedaEndpoint(uri, this, getOrCreateQueue(uri, size), consumers);
        answer.configureProperties(parameters);
        return answer;
    }
    public synchronized BlockingQueue<Exchange> getQueue() {
        if (queue == null) {
            // prefer to lookup queue from component, so if this endpoint is re-created or re-started
            // then the existing queue from the component can be used, so new producers and consumers
            // can use the already existing queue referenced from the component
            if (getComponent() != null) {
                queue = getComponent().getOrCreateQueue(getEndpointUri(), getSize());
            } else {
                // fallback and create queue (as this endpoint has no component)
                queue = createQueue();
            }
        }
        return queue;
    }
    protected BlockingQueue<Exchange> createQueue() {
        if (size > 0) {
            return new LinkedBlockingQueue<Exchange>(size);
        } else {
            return new LinkedBlockingQueue<Exchange>();
        }
    }
    protected void doShutdown() throws Exception {
        // notify component we are shutting down this endpoint
        if (getComponent() != null) {
            getComponent().onShutdownEndpoint(this);
        }
        // shutdown thread pool if it was in use
        if (multicastExecutor != null) {
            getCamelContext().getExecutorServiceManager().shutdownNow(multicastExecutor);
            multicastExecutor = null;
        }

        // clear queue, as we are shutdown, so if re-created then the queue must be updated
        queue = null;

        super.doShutdown();
    }
