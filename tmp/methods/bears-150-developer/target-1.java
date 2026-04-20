    public LocaleHandle(String language, String country, String variant) {
        this.language = language;
        this.country = country;
        this.variant = variant;
    }
    private Object readResolve() {
        return new Locale(language, country, variant);
    }
    public void writeObject(Object obj, AbstractHessianOutput out)
            throws IOException {
        if (obj == null)
            out.writeNull();
        else {
            Locale locale = (Locale) obj;

            out.writeObject(new LocaleHandle(locale.getLanguage(), locale.getCountry(), locale.getVariant()));
        }
    }
