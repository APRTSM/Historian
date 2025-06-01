    public static ByteBuffer toByteBuffer(Float value) {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putFloat(value);
        buf.flip();
        return buf;
    }
    public static ByteBuffer toByteBuffer(Long value) {
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putLong(value);
        buf.flip();
        return buf;
    }
    public static ByteBuffer toByteBuffer(Integer value) {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(value);
        buf.flip();
        return buf;
    }
    public static ByteBuffer toByteBuffer(Double value) {
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putDouble(value);
        buf.flip();
        return buf;
    }
    public static ByteBuffer toByteBuffer(Short value) {
        ByteBuffer buf = ByteBuffer.allocate(2);
        buf.putShort(value);
        buf.flip();
        return buf;
    }
