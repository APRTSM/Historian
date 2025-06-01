    public String getMessage() {
        context.addMessage(LocalizedFormats.ARITHMETIC_EXCEPTION);
		getContext().addMessage(LocalizedFormats.ITERATIONS);
		return context.getMessage();
    }
