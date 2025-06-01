    public static boolean evaluateValuePredicate(Object value) {
        if (value instanceof Boolean) {
            return (Boolean)value;
        } else if (value instanceof String) {
            if ("true".equalsIgnoreCase((String)value)) {
                return true;
            } else if ("false".equalsIgnoreCase((String)value)) {
                return false;
            }
        }
        return value != null;
    }
