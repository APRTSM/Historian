    public static String toString(ByteBuffer buffer, Exchange exchange) throws IOException {
        return IOConverter.toString(buffer.array(), exchange);
    }
