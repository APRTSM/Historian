    public static ChannelBuffer encodeContent(
            boolean connectionless, String uniqueId, int type, int index, ChannelBuffer content) {

        ChannelBuffer buf = ChannelBuffers.dynamicBuffer();

        if (connectionless) {
            buf.writeBytes(ChannelBuffers.wrappedBuffer(DatatypeConverter.parseHexBinary('0' + uniqueId)));
        }

        buf.writeByte(0x67);
        buf.writeByte(0x67);
        buf.writeByte(type);
        buf.writeShort(2 + (content != null ? content.readableBytes() : 0)); // length
        buf.writeShort(index);

        if (content != null) {
            buf.writeBytes(content);
        }

        ChannelBuffer result = ChannelBuffers.dynamicBuffer();

        if (connectionless) {
            result.writeByte('E');
            result.writeByte('L');
            result.writeShort(2 + 2 + 2 + buf.readableBytes()); // length
            result.writeShort(checksum(buf.toByteBuffer()));
        }

        result.writeBytes(buf);

        return result;
    }
