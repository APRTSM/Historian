    public String getLocalizedMessage() {
        context = new HashMap<String, Object>();
		return getMessage(Locale.getDefault());
    }
