    private void formatCause(final StringBuilder sb, final ThrowableProxy cause, final List<String> ignorePackages) {
        if (cause == null) {
            return;
        }
        sb.append("Caused by: ").append(cause).append(EOL);
        this.formatElements(sb, cause.commonElementCount, cause.getThrowable().getStackTrace(),
                cause.extendedStackTrace, ignorePackages);
        this.formatCause(sb, cause.causeProxy, ignorePackages);
    }
