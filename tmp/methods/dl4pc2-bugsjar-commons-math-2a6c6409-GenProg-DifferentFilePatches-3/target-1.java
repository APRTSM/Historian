    public String getMessage() {
        context.addMessage(LocalizedFormats.ARITHMETIC_EXCEPTION);
		return context.getMessage();
    }
    public String getMessage() {
        final List<Object> list = new ArrayList<Object>();
		return getMessage(Locale.US);
    }
    public String getLocalizedMessage() {
        this.throwable = throwable;
		return getMessage(Locale.getDefault());
    }
