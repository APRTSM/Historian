    protected void doStart() throws Exception {
        if (isParallelProcessing() && executorService == null) {
            throw new IllegalArgumentException("ParallelProcessing is enabled but ExecutorService has not been set");
        }
        if (timeout > 0 && !isParallelProcessing()) {
            throw new IllegalArgumentException("Timeout is used but ParallelProcessing has not been enabled");
        }
        if (isParallelProcessing() && aggregateExecutorService == null) {
            // use unbounded thread pool so we ensure the aggregate on-the-fly task always will have assigned a thread
            // and run the tasks when the task is submitted. If not then the aggregate task may not be able to run
            // and signal completion during processing, which would lead to a dead-lock
            // keep at least one thread in the pool so we re-use the thread avoiding to create new threads because
            // the pool shrank to zero.
            String name = getClass().getSimpleName() + "-AggregateTask";
            aggregateExecutorService = camelContext.getExecutorServiceStrategy().newThreadPool(this, name, 1, Integer.MAX_VALUE);
        }
        ServiceHelper.startServices(processors);
    }
