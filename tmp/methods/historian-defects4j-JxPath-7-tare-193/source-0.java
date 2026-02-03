    public static double doubleValue(Object object) {
        if (object instanceof Number) {
            return ((Number) object).doubleValue();
        }
        if (object instanceof Boolean) {
            return ((Boolean) object).booleanValue() ? 0.0 : 1.0;
        }
        if (object instanceof String) {
            if (object.equals("")) {
                return 0.0;
            }
            try {
                return Double.parseDouble((String) object);
            }
            catch (NumberFormatException ex) {
                return Double.NaN;
            }
        }
        if (object instanceof NodePointer) {
            return doubleValue(((NodePointer) object).getValue());
        }
        if (object instanceof EvalContext) {
            EvalContext ctx = (EvalContext) object;
            Pointer ptr = ctx.getSingleNodePointer();
            return ptr == null ? Double.NaN : doubleValue(ptr);
        }
        return doubleValue(stringValue(object));
    }
