        protected Object _deserialize(String value, DeserializationContext ctxt) throws IOException
        {
            switch (_kind) {
            case STD_FILE:
                return new File(value);
            case STD_URL:
                return new URL(value);
            case STD_URI:
                return URI.create(value);
            case STD_CLASS:
                try {
                    return ctxt.findClass(value);
                } catch (Exception e) {
                    return ctxt.handleInstantiationProblem(_valueClass, value,
                            ClassUtil.getRootCause(e));
                }
            case STD_JAVA_TYPE:
                return ctxt.getTypeFactory().constructFromCanonical(value);
            case STD_CURRENCY:
                // will throw IAE if unknown:
                return Currency.getInstance(value);
            case STD_PATTERN:
                // will throw IAE (or its subclass) if malformed
                return Pattern.compile(value);
            case STD_LOCALE:
                {
                    int ix = _firstHyphenOrUnderscore(value);
                    if (ix < 0) { // single argument
                        return new Locale(value);
                    }
                    String first = value.substring(0, ix);
                    value = value.substring(ix+1);
                    ix = _firstHyphenOrUnderscore(value);
                    if (ix < 0) { // two pieces
                        return new Locale(first, value);
                    }
                    String second = value.substring(0, ix);
                    return new Locale(first, second, value.substring(ix+1));
                }
            case STD_CHARSET:
                return Charset.forName(value);
            case STD_TIME_ZONE:
                return TimeZone.getTimeZone(value);
            case STD_INET_ADDRESS:
                return InetAddress.getByName(value);
            case STD_INET_SOCKET_ADDRESS:
                if (value.startsWith("[")) {
                    // bracketed IPv6 (with port number)

                    int i = value.lastIndexOf(']');
                    if (i == -1) {
                        throw new InvalidFormatException(ctxt.getParser(),
                                "Bracketed IPv6 address must contain closing bracket",
                                value, InetSocketAddress.class);
                    }

                    int j = value.indexOf(':', i);
                    int port = j > -1 ? Integer.parseInt(value.substring(j + 1)) : 0;
                    return new InetSocketAddress(value.substring(0, i + 1), port);
                }
                int ix = value.indexOf(':');
                if (ix >= 0 && value.indexOf(':', ix + 1) < 0) {
                    // host:port
                    int port = Integer.parseInt(value.substring(ix+1));
                    return new InetSocketAddress(value.substring(0, ix), port);
                }
                // host or unbracketed IPv6, without port number
                return new InetSocketAddress(value, 0);
            case STD_STRING_BUILDER:
                return new StringBuilder(value);
            }
            VersionUtil.throwInternal();
            return null;
        }
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        // 22-Sep-2012, tatu: For 2.1, use this new method, may force coercion:
        String text = p.getValueAsString();
        if (text != null) { // has String representation
            if (text.length() == 0 || (text = text.trim()).length() == 0) {
                // 04-Feb-2013, tatu: Usually should become null; but not always
                return _deserializeFromEmptyString();
            }
            Exception cause = null;
            try {
                // 19-May-2017, tatu: Used to require non-null result (assuming `null`
                //    indicated error; but that seems wrong. Should be able to return
                //    `null` as value.
                return _deserialize(text, ctxt);
            } catch (IllegalArgumentException iae) {
                cause = iae;
            } catch (MalformedURLException me) {
                cause = me;
            }
            String msg = "not a valid textual representation";
            if (cause != null) {
                String m2 = cause.getMessage();
                if (m2 != null) {
                    msg = msg + ", problem: "+m2;
                }
            }
            // 05-May-2016, tatu: Unlike most usage, this seems legit, so...
            JsonMappingException e = ctxt.weirdStringException(text, _valueClass, msg);
            if (cause != null) {
                e.initCause(cause);
            }
            throw e;
            // nothing to do here, yet? We'll fail anyway
        }
        JsonToken t = p.getCurrentToken();
        // [databind#381]
        if (t == JsonToken.START_ARRAY) {
            return _deserializeFromArray(p, ctxt);
        }
        if (t == JsonToken.VALUE_EMBEDDED_OBJECT) {
            // Trivial cases; null to null, instance of type itself returned as is
            Object ob = p.getEmbeddedObject();
            if (ob == null) {
                return null;
            }
            if (_valueClass.isAssignableFrom(ob.getClass())) {
                return (T) ob;
            }
            return _deserializeEmbedded(ob, ctxt);
        }
        return (T) ctxt.handleUnexpectedToken(_valueClass, p);
    }
