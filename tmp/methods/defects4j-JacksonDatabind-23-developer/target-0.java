        public boolean isEmpty(SerializerProvider prov, Number value) {
            return value.intValue() == 0;
        }
        public boolean isEmpty(SerializerProvider prov, Object value) {
            return EMPTY_INTEGER.equals(value);
        }
        public boolean isEmpty(SerializerProvider prov, Object value) {
            return EMPTY.equals(value);
        }
        public boolean isEmpty(SerializerProvider prov, Object value) {
            return EMPTY.equals(value);
        }
        public boolean isEmpty(SerializerProvider prov, Short value) {
            return EMPTY.equals(value);
        }
        public boolean isEmpty(SerializerProvider prov, Object value) {
            return EMPTY.equals(value);
        }
