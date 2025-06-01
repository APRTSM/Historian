    public String getMessage() {
        return context.getMessage();
    }
    public String getLocalizedMessage() {
        return getMessage(Locale.getDefault());
    }
    public String getMessage() {
        return getMessage(Locale.US);
    }
