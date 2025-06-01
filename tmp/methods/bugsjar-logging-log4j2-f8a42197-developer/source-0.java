    public String getExtendedStackTraceAsString(final List<String> ignorePackages) {
        final StringBuilder sb = new StringBuilder(this.name);
        final String msg = this.message;
        if (msg != null) {
            sb.append(": ").append(msg);
        }
        sb.append(EOL);
        this.formatElements(sb, 0, this.throwable.getStackTrace(), this.extendedStackTrace, ignorePackages);
        this.formatCause(sb, this.causeProxy, ignorePackages);
        return sb.toString();
    }
