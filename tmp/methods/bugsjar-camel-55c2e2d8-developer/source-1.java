    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        int consumers = getAndRemoveParameter(parameters, "concurrentConsumers", Integer.class, defaultConcurrentConsumers);
        boolean limitConcurrentConsumers = getAndRemoveParameter(parameters, "limitConcurrentConsumers", Boolean.class, true);
        if (limitConcurrentConsumers && consumers >  maxConcurrentConsumers) {
            throw new IllegalArgumentException("The limitConcurrentConsumers flag in set to true. ConcurrentConsumers cannot be set at a value greater than "
                    + maxConcurrentConsumers + " was " + consumers);
        }
        SedaEndpoint answer = new SedaEndpoint(uri, this, createQueue(uri, parameters), consumers);
        answer.configureProperties(parameters);
        return answer;
    }
    public synchronized BlockingQueue<Exchange> createQueue(String uri, Map<String, Object> parameters) {
        String key = getQueueKey(uri);

        QueueReference ref = getQueues().get(key);
        if (ref != null) {
            // add the reference before returning queue
            ref.addReference();
            return ref.getQueue();
        }

        // create queue
        BlockingQueue<Exchange> queue;
        Integer size = getAndRemoveParameter(parameters, "size", Integer.class);
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
        super.doShutdown();
    }
    public synchronized BlockingQueue<Exchange> getQueue() {
        if (queue == null) {
            if (size > 0) {
                queue = new LinkedBlockingQueue<Exchange>(size);
            } else {
                queue = new LinkedBlockingQueue<Exchange>();
            }
        }
        return queue;
    }
