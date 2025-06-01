    public FormattedMessage(final String messagePattern, final Object arg) {
        this(messagePattern, new Object[] {arg}, null);
    }
    public FormattedMessage(final String messagePattern, final Object[] arguments, final Throwable throwable) {
        this.messagePattern = messagePattern;
        this.argArray = arguments;
        this.throwable = throwable;
        getFormattedMessage(); // LOG4J2-763 take snapshot of parameters at message construction time
    }
    public FormattedMessage(final String messagePattern, final Object[] arguments) {
        this(messagePattern, arguments, null);
    }
