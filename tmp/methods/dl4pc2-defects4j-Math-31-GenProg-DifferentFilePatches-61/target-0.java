    public String getLocalizedMessage() {
        final List<Object> list = new ArrayList<Object>();
		return getMessage(Locale.getDefault());
    }
