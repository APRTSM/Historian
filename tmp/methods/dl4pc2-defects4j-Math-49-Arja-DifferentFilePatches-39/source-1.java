    public String getLocalizedString(final Locale locale) {
        try {
            ResourceBundle bundle =
                    ResourceBundle.getBundle("META-INF/localization/LocalizedFormats", locale);
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
    private double doRemove(int index) {
        keys[index]   = 0;
        states[index] = REMOVED;
        final double previous = values[index];
        values[index] = missingEntries;
        --size;
        ++count;
        return previous;
    }
    public boolean containsKey(final int key) {

        final int hash  = hashOf(key);
        int index = hash & mask;
        if (containsKey(key, index)) {
            return true;
        }

        if (states[index] == FREE) {
            return false;
        }

        int j = index;
        for (int perturb = perturb(hash); states[index] != FREE; perturb >>= PERTURB_SHIFT) {
            j = probe(perturb, j);
            index = j & mask;
            if (containsKey(key, index)) {
                return true;
            }
        }

        return false;

    }
