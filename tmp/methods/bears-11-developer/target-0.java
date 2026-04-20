    public void serialize(Object value, JsonGenerator g, SerializerProvider provider) throws IOException {
        String str;
        Class<?> cls = value.getClass();

        if (cls == String.class) {
            str = (String) value;
        } else if (cls.isEnum()) {
            // 24-Sep-2015, tatu: Minor improvement over older (2.6.2 and before) code: at least
            //     use name/toString() variation for as per configuration
            if (provider.isEnabled(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)) {
                str = value.toString();
            } else {
                Enum<?> en = (Enum<?>) value;
                if (provider.isEnabled(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)) {
                    str = String.valueOf(en.ordinal());
                } else {
                    str = en.name();
                }
            }
        } else if (value instanceof Date) {
            provider.defaultSerializeDateKey((Date) value, g);
            return;
        } else if (cls == Class.class) {
            str = ((Class<?>) value).getName();
        } else {
            str = value.toString();
        }
        g.writeFieldName(str);
    }
