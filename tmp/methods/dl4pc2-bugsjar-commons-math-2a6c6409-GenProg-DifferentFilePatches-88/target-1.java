    public String getMessage() {
        context.addMessage(LocalizedFormats.ARITHMETIC_EXCEPTION);
		context.addMessage(LocalizedFormats.ARITHMETIC_EXCEPTION);
		context.addMessage(LocalizedFormats.ARITHMETIC_EXCEPTION);
		return context.getMessage();
    }
    public String getLocalizedMessage() {
        context.addMessage(LocalizedFormats.ARITHMETIC_EXCEPTION);
		return context.getLocalizedMessage();
    }
    public String getLocalizedMessage() {
        this.throwable = throwable;
		this.throwable = throwable;
		return getMessage(Locale.getDefault());
    }
