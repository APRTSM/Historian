    public ThreadsDefinition threads(int poolSize, int maxPoolSize) {
        ThreadsDefinition answer = threads();
        answer.setPoolSize(poolSize);
        answer.setMaxPoolSize(maxPoolSize);
        addOutput(answer);
        return answer;
    }
    public ThreadsDefinition threads(int poolSize, int maxPoolSize, String threadName) {
        ThreadsDefinition answer = threads();
        answer.setPoolSize(poolSize);
        answer.setMaxPoolSize(maxPoolSize);
        answer.setThreadName(threadName);
        addOutput(answer);
        return answer;
    }
    public ThreadsDefinition threads(int poolSize) {
        ThreadsDefinition answer = threads();
        answer.setPoolSize(poolSize);
        addOutput(answer);
        return answer;
    }
