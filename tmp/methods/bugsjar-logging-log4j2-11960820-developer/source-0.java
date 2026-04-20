    private ThrowableProxy(final Throwable parent, final Stack<Class<?>> stack, final Map<String, CacheEntry> map,
            final Throwable cause, Set<Throwable> suppressedVisited) {
        this.throwable = cause;
        this.name = cause.getClass().getName();
        this.message = this.throwable.getMessage();
        this.localizedMessage = this.throwable.getLocalizedMessage();
        this.extendedStackTrace = this.toExtendedStackTrace(stack, map, parent.getStackTrace(), cause.getStackTrace());
        this.causeProxy = cause.getCause() == null ? null : new ThrowableProxy(parent, stack, map, cause.getCause(), suppressedVisited);
        this.suppressedProxies = this.toSuppressedProxies(cause, suppressedVisited);
    }
    public ThrowableProxy(final Throwable throwable, Set<Throwable> visited) {
        this.throwable = throwable;
        this.name = throwable.getClass().getName();
        this.message = throwable.getMessage();
        this.localizedMessage = throwable.getLocalizedMessage();
        final Map<String, CacheEntry> map = new HashMap<>();
        final Stack<Class<?>> stack = ReflectionUtil.getCurrentStackTrace();
        this.extendedStackTrace = this.toExtendedStackTrace(stack, map, null, throwable.getStackTrace());
        final Throwable throwableCause = throwable.getCause();
        this.causeProxy = throwableCause == null ? null : new ThrowableProxy(throwable, stack, map, throwableCause, visited);
        this.suppressedProxies = this.toSuppressedProxies(throwable, visited);
    }
