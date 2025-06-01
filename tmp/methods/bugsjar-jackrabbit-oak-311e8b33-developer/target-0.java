    private synchronized RecordId writeValueRecord(String reference) {
        byte[] data = reference.getBytes(Charsets.UTF_8);
        int length = data.length;

        // When writing a binary ID, the four most significant bits of the
        // length field should be "1110", leaving 12 other bits to store the
        // length itself. This means that the values of the length field can
        // only range between 0 and 2^12 - 1.

        checkArgument(length < 4096);

        RecordId id = prepare(RecordType.VALUE, 2 + length);
        int len = length | 0xE000;
        buffer[position++] = (byte) (len >> 8);
        buffer[position++] = (byte) len;

        System.arraycopy(data, 0, buffer, position, length);
        position += length;

        blobrefs.add(id);
        return id;
    }
