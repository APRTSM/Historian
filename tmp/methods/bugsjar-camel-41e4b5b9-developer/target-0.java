    public static boolean evaluateValuePredicate(Object value) {
        if (value instanceof Boolean) {
            return (Boolean)value;
        } else if (value instanceof String) {
            if ("true".equalsIgnoreCase((String)value)) {
                return true;
            } else if ("false".equalsIgnoreCase((String)value)) {
                return false;
            }
        } else if (value instanceof NodeList) {
            // is it an empty dom
            NodeList list = (NodeList) value;
            return list.getLength() > 0;
        } else if (value instanceof Collection) {
            // is it an empty collection
            Collection col = (Collection) value;
            return col.size() > 0;
        }
        return value != null;
    }
