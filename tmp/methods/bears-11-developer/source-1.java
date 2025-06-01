    public void serialize(Object value, JsonGenerator g, SerializerProvider provider) throws IOException {
        String str;
        Class<?> cls = value.getClass();

        if (cls == String.class) {
            str = (String) value;
        } else if (cls.isEnum()) {
            // 24-Sep-2015, tatu: Minor improvement over older (2.6.2 and before) code: at least
            //     use name/toString() variation for as per configuration
            Enum<?> en = (Enum<?>) value;

            if (provider.isEnabled(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)) {
                str = en.toString();
            } else {
                str = en.name();
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
        public void serialize(Object value, JsonGenerator g, SerializerProvider provider) throws IOException {
            switch (_typeId) {
            case TYPE_DATE:
                provider.defaultSerializeDateKey((Date)value, g);
                break;
            case TYPE_CALENDAR:
                provider.defaultSerializeDateKey(((Calendar) value).getTimeInMillis(), g);
                break;
            case TYPE_CLASS:
                g.writeFieldName(((Class<?>)value).getName());
                break;
            case TYPE_ENUM:
                {
                    String str = provider.isEnabled(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                            ? value.toString() : ((Enum<?>) value).name();
                    g.writeFieldName(str);
                }
                break;
            case TYPE_TO_STRING:
            default:
                g.writeFieldName(value.toString());
            }
        }
        public void serialize(Object value, JsonGenerator g, SerializerProvider serializers)
                throws IOException
        {
            if (serializers.isEnabled(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)) {
                g.writeFieldName(value.toString());
                return;
            }
            Enum<?> en = (Enum<?>) value;
            g.writeFieldName(_values.serializedValueFor(en));
        }
