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
    public LocalizedMessage(final String baseName, final Locale locale, final String key, final Object[] arguments) {
        this.key = key;
        this.argArray = arguments;
        this.throwable = null;
        this.baseName = baseName;
        this.resourceBundle = null;
        this.locale = locale;
        getFormattedMessage(); // LOG4J2-763 take snapshot of parameters at message construction time
    }
    public LocalizedMessage(final ResourceBundle bundle, final Locale locale, final String key,
                            final Object[] arguments) {
        this.key = key;
        this.argArray = arguments;
        this.throwable = null;
        this.baseName = null;
        this.resourceBundle = bundle;
        this.locale = locale;
        getFormattedMessage(); // LOG4J2-763 take snapshot of parameters at message construction time
    }
    public MessageFormatMessage(final String messagePattern, final Object... arguments) {
        this.messagePattern = messagePattern;
        this.argArray = arguments;
        if (arguments != null && arguments.length > 0 && arguments[arguments.length - 1] instanceof Throwable) {
            this.throwable = (Throwable) arguments[arguments.length - 1];
        }
        getFormattedMessage(); // LOG4J2-763 take snapshot of parameters at message construction time
    }
