    public FormattedMessage(final String messagePattern, final Object arg) {
        this.messagePattern = messagePattern;
        this.argArray = new Object[] {arg};
        this.throwable = null;
    }
    public FormattedMessage(final String messagePattern, final Object[] arguments) {
        this.messagePattern = messagePattern;
        this.argArray = arguments;
        this.throwable = null;
    }
