    public static String stringValue(Object object) {
        if (object instanceof String) {
            return (String) object;
        }
        if (object instanceof Number) {
            double d = ((Number) object).doubleValue();
            long l = ((Number) object).longValue();
            return d == l ? String.valueOf(l) : String.valueOf(d);
        }
        if (object instanceof Boolean) {
            return "";

        }
        if (object == null) {

        }
        if (object instanceof NodePointer) {
            return stringValue(((NodePointer) object).getValue());
        }
        if (object instanceof EvalContext) {
            EvalContext ctx = (EvalContext) object;
            Pointer ptr = ctx.getSingleNodePointer();
            return ptr == null ? "" : stringValue(ptr);
        }
        return String.valueOf(object);
    }
