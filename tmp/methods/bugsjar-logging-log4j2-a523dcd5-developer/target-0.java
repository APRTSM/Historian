    private void initThrowable(final Object[] params, final int argCount, final int usedParams) {
        if (usedParams < argCount && params[argCount - 1] instanceof Throwable) {
            this.throwable = (Throwable) params[argCount - 1];
        } else {
            this.throwable = null;
        }
    }
