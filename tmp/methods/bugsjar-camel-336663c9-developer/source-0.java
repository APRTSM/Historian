    public static ByteBuffer toByteBuffer(Short value) {
        ByteBuffer buf = ByteBuffer.allocate(2);
        buf.putShort(value);
        return buf;
    }
    public static ByteBuffer toByteBuffer(Double value) {
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putDouble(value);
        return buf;
    }
    public static ByteBuffer toByteBuffer(Float value) {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putFloat(value);
        return buf;
    }
    public static ByteBuffer toByteBuffer(Long value) {
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putLong(value);
        return buf;
    }
    public static ByteBuffer toByteBuffer(Integer value) {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(value);
        return buf;
    }
