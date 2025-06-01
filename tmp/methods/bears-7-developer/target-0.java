    private final Object _deserializeAltString(JsonParser p, DeserializationContext ctxt,
            CompactStringObjectMap lookup, String name) throws IOException
    {
        name = name.trim();
        if (name.length() == 0) {
            if (ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)) {
                return null;
            }
        } else if (!ctxt.isEnabled(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)) {
            // [databind#149]: Allow use of 'String' indexes as well -- unless prohibited (as per above)
            char c = name.charAt(0);
            if (c >= '0' && c <= '9') {
                try {
                    int index = Integer.parseInt(name);
                    if (index >= 0 && index < _enumsByIndex.length) {
                        return _enumsByIndex[index];
                    }
                } catch (NumberFormatException e) {
                    // fine, ignore, was not an integer
                }
            }
        }
        if ((_enumDefaultValue != null)
                && ctxt.isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)) {
            return _enumDefaultValue;
        }
        if (!ctxt.isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)) {
            return ctxt.handleWeirdStringValue(_enumClass(), name,
                    "value not one of declared Enum instance names: %s", lookup.keys());
        }
        return null;
    }
