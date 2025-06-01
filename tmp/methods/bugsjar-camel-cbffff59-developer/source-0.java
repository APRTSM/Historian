    public static Long toLong(Object value) {
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Number) {
            Number number = (Number) value;
            return number.longValue();
        } else if (value instanceof String) {
            return Long.valueOf((String) value);
        } else {
            return null;
        }
    }
    public static Float toFloat(Object value) {
        if (value instanceof Float) {
            return (Float) value;
        } else if (value instanceof Number) {
            Number number = (Number) value;
            return number.floatValue();
        } else if (value instanceof String) {
            return Float.valueOf((String) value);
        } else {
            return null;
        }
    }
    public static Short toShort(Object value) {
        if (value instanceof Short) {
            return (Short) value;
        } else if (value instanceof Number) {
            Number number = (Number) value;
            return number.shortValue();
        } else if (value instanceof String) {
            return Short.valueOf((String) value);
        } else {
            return null;
        }
    }
    public static Integer toInteger(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            Number number = (Number) value;
            return number.intValue();
        } else if (value instanceof String) {
            return Integer.valueOf((String) value);
        } else {
            return null;
        }
    }
