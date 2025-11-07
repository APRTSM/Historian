    @Override public void writeRawValue(String text, int offset, int len) throws IOException {
        offset = 0;
_verifyValueWrite("write raw value");
        writeRaw(text, offset, len);
    }
