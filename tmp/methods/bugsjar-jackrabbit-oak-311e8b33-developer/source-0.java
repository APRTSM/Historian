    private synchronized RecordId writeValueRecord(String reference) {
        byte[] data = reference.getBytes(Charsets.UTF_8);
        int length = data.length;

        checkArgument(length < 8192);

        RecordId id = prepare(RecordType.VALUE, 2 + length);
        int len = length | 0xE000;
        buffer[position++] = (byte) (len >> 8);
        buffer[position++] = (byte) len;

        System.arraycopy(data, 0, buffer, position, length);
        position += length;

        blobrefs.add(id);
        return id;
    }
