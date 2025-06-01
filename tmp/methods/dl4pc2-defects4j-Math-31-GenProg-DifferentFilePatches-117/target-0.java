    public String getMessage() {
        final String path = LocalizedFormats.class.getName().replaceAll("\\.",
				"/");
		return getMessage(Locale.US);
    }
