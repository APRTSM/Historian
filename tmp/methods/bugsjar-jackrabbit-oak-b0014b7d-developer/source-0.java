    public static int getGcGen(ByteBuffer data) {
        return data.getInt(GC_GEN_OFFSET);
    }
    public int getGcGen() {
        return getGcGen(data);
    }
