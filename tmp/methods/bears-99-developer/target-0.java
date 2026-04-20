    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ChannelBuffer buf) throws Exception {

        char marker = (char) buf.getByte(buf.readerIndex());

        while (marker != '*' && marker != '$' && marker != 'X' && buf.readableBytes() > 0) {
            buf.skipBytes(1);
            if (buf.readableBytes() > 0) {
                marker = (char) buf.getByte(buf.readerIndex());
            }
        }

        if (marker == '*') {

            // Return text message
            int index = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) '#');
            if (index != -1) {
                return buf.readBytes(index + 1 - buf.readerIndex());
            }

        } else if (marker == '$') {

            if (messageLength == 0) {
                if (buf.readableBytes() == MESSAGE_LONG) {
                    messageLength = MESSAGE_LONG;
                } else {
                    messageLength = MESSAGE_SHORT;
                }
            }

            if (buf.readableBytes() >= messageLength) {
                return buf.readBytes(messageLength);
            }

        } else if (marker == 'X') {

            if (buf.readableBytes() >= MESSAGE_SHORT) {
                return buf.readBytes(MESSAGE_SHORT);
            }

        }

        return null;
    }
