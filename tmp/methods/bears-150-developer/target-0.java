    public LocaleHandle(String language, String country, String variant) {
        this.language = language;
        this.country = country;
        this.variant = variant;
    }
    private Object readResolve() {
        return new Locale(language, country, variant);
    }
