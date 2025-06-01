    public String getMessage() {
        context.addMessage(LocalizedFormats.ARITHMETIC_EXCEPTION);
		context.addMessage(LocalizedFormats.ARITHMETIC_EXCEPTION);
		context.addMessage(LocalizedFormats.ARITHMETIC_EXCEPTION);
		return context.getMessage();
    }
    public String getLocalizedMessage() {
        getContext().addMessage(LocalizedFormats.EVALUATIONS);
		return context.getLocalizedMessage();
    }
