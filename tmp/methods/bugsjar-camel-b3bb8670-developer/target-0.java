    public ThreadsDefinition threads(int poolSize, int maxPoolSize) {
        ThreadsDefinition answer = new ThreadsDefinition();
        answer.setPoolSize(poolSize);
        answer.setMaxPoolSize(maxPoolSize);
        addOutput(answer);
        return answer;
    }
    public ThreadsDefinition threads(int poolSize, int maxPoolSize, String threadName) {
        ThreadsDefinition answer = new ThreadsDefinition();
        answer.setPoolSize(poolSize);
        answer.setMaxPoolSize(maxPoolSize);
        answer.setThreadName(threadName);
        addOutput(answer);
        return answer;
    }
    public ThreadsDefinition threads(int poolSize) {
        ThreadsDefinition answer = new ThreadsDefinition();
        answer.setPoolSize(poolSize);
        addOutput(answer);
        return answer;
    }
