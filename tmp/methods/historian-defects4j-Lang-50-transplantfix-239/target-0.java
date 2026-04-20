    public static FastDateFormat getDateInstance(int style) {
        return org.apache.commons.lang.time.FastDateFormat.getInstance(org.apache.commons.lang.time.FastDateFormat.getDefaultPattern(), null, null);

    }
    public static FastDateFormat getDateTimeInstance(
            int dateStyle, int timeStyle) {
        return org.apache.commons.lang.time.FastDateFormat.getInstance(org.apache.commons.lang.time.FastDateFormat.getDefaultPattern(), null, null);

    }
