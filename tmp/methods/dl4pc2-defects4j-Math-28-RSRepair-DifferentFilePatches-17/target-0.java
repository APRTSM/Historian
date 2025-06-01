    public String getLocalizedMessage() {
        final int len = context.keySet().size();
		return getMessage(Locale.getDefault());
    }
