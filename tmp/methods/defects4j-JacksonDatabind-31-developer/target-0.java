    public void writeRawValue(String text, int offset, int len) throws IOException {
        if (offset > 0 || len != text.length()) {
            text = text.substring(offset, offset+len);
        }
        _appendValue(JsonToken.VALUE_EMBEDDED_OBJECT, new RawValue(text));
    }
    public void writeNumber(short i) throws IOException {
        _appendValue(JsonToken.VALUE_NUMBER_INT, Short.valueOf(i));
    }
    public void writeNumber(BigDecimal dec) throws IOException {
        if (dec == null) {
            writeNull();
        } else {
            _appendValue(JsonToken.VALUE_NUMBER_FLOAT, dec);
        }
    }
    public void writeString(SerializableString text) throws IOException {
        if (text == null) {
            writeNull();
        } else {
            _appendValue(JsonToken.VALUE_STRING, text);
        }
    }
    public void writeNumber(BigInteger v) throws IOException {
        if (v == null) {
            writeNull();
        } else {
            _appendValue(JsonToken.VALUE_NUMBER_INT, v);
        }
    }
    public void writeBoolean(boolean state) throws IOException {
        _appendValue(state ? JsonToken.VALUE_TRUE : JsonToken.VALUE_FALSE);
    }
    public void writeString(String text) throws IOException {
        if (text == null) {
            writeNull();
        } else {
            _appendValue(JsonToken.VALUE_STRING, text);
        }
    }
    public void writeTree(TreeNode node) throws IOException
    {
        if (node == null) {
            writeNull();
            return;
        }

        if (_objectCodec == null) {
            // as with 'writeObject()', is codec optional?
            _appendValue(JsonToken.VALUE_EMBEDDED_OBJECT, node);
        } else {
            _objectCodec.writeTree(this, node);
        }
    }
    public void writeNumber(int i) throws IOException {
        _appendValue(JsonToken.VALUE_NUMBER_INT, Integer.valueOf(i));
    }
    public void writeNumber(long l) throws IOException {
        _appendValue(JsonToken.VALUE_NUMBER_INT, Long.valueOf(l));
    }
    protected final void _appendValue(JsonToken type)
    {
        _writeContext.writeValue();
        Segment next = _hasNativeId
                ? _last.append(_appendAt, type, _objectId, _typeId)
                : _last.append(_appendAt, type);
        if (next == null) {
            ++_appendAt;
        } else {
            _last = next;
            _appendAt = 1; // since we added first at 0
        }
    }
    public void writeNumber(double d) throws IOException {
        _appendValue(JsonToken.VALUE_NUMBER_FLOAT, Double.valueOf(d));
    }
    public void writeNull() throws IOException {
        _appendValue(JsonToken.VALUE_NULL);
    }
    public void writeNumber(float f) throws IOException {
        _appendValue(JsonToken.VALUE_NUMBER_FLOAT, Float.valueOf(f));
    }
    public void writeNumber(String encodedValue) throws IOException {
        /* 03-Dec-2010, tatu: related to [JACKSON-423], should try to keep as numeric
         *   identity as long as possible
         */
        _appendValue(JsonToken.VALUE_NUMBER_FLOAT, encodedValue);
    }
    public void writeRawValue(char[] text, int offset, int len) throws IOException {
        _appendValue(JsonToken.VALUE_EMBEDDED_OBJECT, new String(text, offset, len));
    }
    public void writeObject(Object value) throws IOException
    {
        if (value == null) {
            writeNull();
            return;
        }
        Class<?> raw = value.getClass();
        if (raw == byte[].class || (value instanceof RawValue)) {
            _appendValue(JsonToken.VALUE_EMBEDDED_OBJECT, value);
            return;
        }
        if (_objectCodec == null) {
            /* 28-May-2014, tatu: Tricky choice here; if no codec, should we
             *   err out, or just embed? For now, do latter.
             */
//          throw new JsonMappingException("No ObjectCodec configured for TokenBuffer, writeObject() called");
            _appendValue(JsonToken.VALUE_EMBEDDED_OBJECT, value);
        } else {
            _objectCodec.writeValue(this, value);
        }
    }
    protected final void _appendValue(JsonToken type, Object value)
    {
        _writeContext.writeValue();
        Segment next = _hasNativeId
                ? _last.append(_appendAt, type, value, _objectId, _typeId)
                : _last.append(_appendAt, type, value);
        if (next == null) {
            ++_appendAt;
        } else {
            _last = next;
            _appendAt = 1;
        }
    }
    public void writeRawValue(String text) throws IOException {
        _appendValue(JsonToken.VALUE_EMBEDDED_OBJECT, new RawValue(text));
    }
