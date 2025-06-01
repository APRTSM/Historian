    public static ByteBuffer toByteBuffer(String value, Exchange exchange) {
        ByteBuffer buf = ByteBuffer.allocate(value.length());
        byte[] bytes = null;
        if (exchange != null) {
            String charsetName = exchange.getProperty(Exchange.CHARSET_NAME, String.class);
            if (charsetName != null) {
                try {
                    bytes = value.getBytes(charsetName);
                } catch (UnsupportedEncodingException e) {
                    LOG.warn("Cannot convert the byte to String with the charset " + charsetName, e);
                }
            }
        }
        if (bytes == null) {
            bytes = value.getBytes();
        }
        buf.put(bytes);
        return buf;
    }
