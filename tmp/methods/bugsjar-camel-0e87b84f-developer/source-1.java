    protected Object invokeWithbody(final Method method, Object body, final ExchangePattern pattern) throws InterruptedException, Throwable {
        final Exchange exchange = new DefaultExchange(endpoint, pattern);
        exchange.getIn().setBody(body);

        // is the return type a future
        final boolean isFuture = method.getReturnType() == Future.class;

        // create task to execute the proxy and gather the reply
        FutureTask<Object> task = new FutureTask<Object>(new Callable<Object>() {
            public Object call() throws Exception {
                // process the exchange
                LOG.trace("Proxied method call {} invoking producer: {}", method.getName(), producer);
                producer.process(exchange);

                Object answer = afterInvoke(method, exchange, pattern, isFuture);
                LOG.trace("Proxied method call {} returning: {}", method.getName(), answer);
                return answer;
            }
        });

        if (isFuture) {
            // submit task and return future
            if (LOG.isTraceEnabled()) {
                LOG.trace("Submitting task for exchange id {}", exchange.getExchangeId());
            }
            getExecutorService(exchange.getContext()).submit(task);
            return task;
        } else {
            // execute task now
            try {
                task.run();
                return task.get();
            } catch (ExecutionException e) {
                // we don't want the wrapped exception from JDK
                throw e.getCause();
            }
        }
    }
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        BeanInvocation invocation = new BeanInvocation(method, args);
        MethodInfo methodInfo = methodInfoCache.getMethodInfo(method);
        final ExchangePattern pattern = methodInfo != null ? methodInfo.getPattern() : ExchangePattern.InOut;
        return invokeWithbody(method, invocation, pattern);
    }
