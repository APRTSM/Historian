    public static synchronized FastDateFormat getDateInstance(int style, TimeZone timeZone, Locale locale) {
        Object key = new Integer(style);
        if (timeZone != null) {
            key = new Pair(key, timeZone);
        }

        if (locale != null) {
            key = new Pair(key, locale);
        }


        FastDateFormat format = (FastDateFormat) cDateTimeInstanceCache
				.get(key);
        if (format == null) {
            if (locale == null) {
                locale = Locale.getDefault();
            }
            try {
                SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateInstance(style, locale);
                String pattern = formatter.toPattern();
                format = getInstance(pattern, timeZone, locale);
                cDateInstanceCache.put(key, format);
                
            } catch (ClassCastException ex) {
                throw new IllegalArgumentException("No date pattern for locale: " + locale);
            }
        }
        return format;
    }
    public static FastDateFormat getDateTimeInstance(
            int dateStyle, int timeStyle, Locale locale) {
        if (locale == null) {
					locale = Locale.getDefault();
				}
		return getDateTimeInstance(dateStyle, timeStyle, null, locale);
    }
    public static FastDateFormat getDateTimeInstance(
            int dateStyle, int timeStyle) {
        return getInstance(getDefaultPattern(), null, null);
    }
