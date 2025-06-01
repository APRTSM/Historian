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
    public LocalizedMessage(final ResourceBundle bundle, final Locale locale, final String key,
                            final Object[] arguments) {
        this.key = key;
        this.argArray = arguments;
        this.throwable = null;
        this.baseName = null;
        this.resourceBundle = bundle;
        this.locale = locale;
    }
    public LocalizedMessage(final String baseName, final Locale locale, final String key, final Object[] arguments) {
        this.key = key;
        this.argArray = arguments;
        this.throwable = null;
        this.baseName = baseName;
        this.resourceBundle = null;
        this.locale = locale;
    }
    public MessageFormatMessage(final String messagePattern, final Object... arguments) {
        this.messagePattern = messagePattern;
        this.argArray = arguments;
        if (arguments != null && arguments.length > 0 && arguments[arguments.length - 1] instanceof Throwable) {
            this.throwable = (Throwable) arguments[arguments.length - 1];
        }
    }
    public String getFormattedMessage() {
        return obj.toString();
    }
    public ObjectMessage(Object obj) {
        if (obj == null) {
            obj = "null";
        }
        this.obj = obj;
    }
    public String getFormat() {
        return obj.toString();
    }
    public String toString() {
        return "ObjectMessage[obj=" + obj.toString() + ']';
    }
    public StringFormattedMessage(final String messagePattern, final Object... arguments) {
        this.messagePattern = messagePattern;
        this.argArray = arguments;
        if (arguments != null && arguments.length > 0 && arguments[arguments.length - 1] instanceof Throwable) {
            this.throwable = (Throwable) arguments[arguments.length - 1];
        }
    }
