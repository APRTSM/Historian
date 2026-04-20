    public double getNumericalVariance() {
        if (!numericalVarianceIsCalculated) {
            numericalVariance = calculateNumericalVariance();
            numericalVarianceIsCalculated = true;
        }
        return numericalVariance;
    }
    private String buildMessage(Locale locale,
                                String separator) {
        final StringBuilder sb = new StringBuilder();
        int count = 0;
        final int len = msgPatterns.size();
        for (int i = 0; i < len; i++) {
            final Localizable pat = msgPatterns.get(i);
            final Object[] args = msgArguments.get(i);
            final MessageFormat fmt = new MessageFormat(pat.getLocalizedString(locale),
                                                        locale);
            sb.append(fmt.format(args));
            if (++count < len) {
                // Add a separator if there are other messages.
                sb.append(separator);
            }
        }

        return sb.toString();
    }
    public String getMessage() {
        return getMessage(Locale.US);
    }
    public String getLocalizedString(final Locale locale) {
        try {
            final String path = LocalizedFormats.class.getName().replaceAll("\\.", "/");
            ResourceBundle bundle =
                    ResourceBundle.getBundle("assets/" + path, locale);
            if (bundle.getLocale().getLanguage().equals(locale.getLanguage())) {
                // the value of the resource is the translated format
                return bundle.getString(toString());
            }

        } catch (MissingResourceException mre) {
            // do nothing here
        }

        // either the locale is not supported or the resource is unknown
        // don't translate and fall back to using the source format
        return sourceFormat;

    }
