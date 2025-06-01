    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ChannelBuffer buf) throws Exception {

        // Skip Alive message
        while (buf.readable() && Character.isDigit(buf.getByte(buf.readerIndex()))) {
            buf.readByte();
        }

        // Check minimum length
        if (buf.readableBytes() < 11) {
            return null;
        }

        // Read flags
        int version = buf.getUnsignedByte(buf.readerIndex() + 1);
        int offset = 1 + 1 + 3;
        if ((version & 0x80) != 0) {
            offset += 4;
        }

        // Get data length
        int length = buf.getUnsignedShort(buf.readerIndex() + offset);
        offset += 2;
        if ((version & 0x40) != 0) {
            offset += 3;
        }
        length += offset; // add header

        // Return buffer
        if (buf.readableBytes() >= length) {
            return buf.readBytes(length);
        }

        return null;
    }
