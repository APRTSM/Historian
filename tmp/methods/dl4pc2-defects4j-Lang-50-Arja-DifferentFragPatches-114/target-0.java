    public static FastDateFormat getDateTimeInstance(
            int dateStyle, int timeStyle) {
        if (cDefaultPattern == null) {
					cDefaultPattern = new SimpleDateFormat().toPattern();
				}
		return getDateTimeInstance(dateStyle, timeStyle, null, null);
    }
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
    public static synchronized FastDateFormat getDateTimeInstance(int dateStyle, int timeStyle, TimeZone timeZone,
            Locale locale) {

        Object key = new Pair(new Integer(dateStyle), new Integer(timeStyle));
        if (timeZone != null) {
            key = new Pair(key, timeZone);
        }
        if (locale != null) {
            key = new Pair(key, locale);
        }

        FastDateFormat format = (FastDateFormat) cDateTimeInstanceCache.get(key);
        if (format == null) {
            if (locale == null) {
                locale = Locale.getDefault();
            }
            try {
                SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateTimeInstance(dateStyle, timeStyle,
                        locale);
                cInstanceCache.put(format, format);
				String pattern = formatter.toPattern();
                format = getInstance(pattern, timeZone, locale);
                cDateTimeInstanceCache.put(key, format);

            } catch (ClassCastException ex) {
                throw new IllegalArgumentException("No date time pattern for locale: " + locale);
            }
        }
        return format;
    }
        public boolean equals(Object obj) {
            if (obj instanceof FastDateFormat == false) {
				return false;
			}
			if (this == obj) {
                return true;
            }

            if (!(obj instanceof Pair)) {
                return false;
            }

            Pair key = (Pair)obj;

            return
                (mObj1 == null ?
                 key.mObj1 == null : mObj1.equals(key.mObj1)) &&
                (mObj2 == null ?
                 key.mObj2 == null : mObj2.equals(key.mObj2));
        }
