    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ChannelBuffer buf) throws Exception {

        if (buf.readableBytes() < 10) {
            return null;
        }

        int beginIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), new StringFinder("$$"));
        if (beginIndex == -1) {
            return null;
        } else if (beginIndex > buf.readerIndex()) {
            buf.readerIndex(beginIndex);
        }

        int length;

        int separatorIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), (byte) '|');
        if (separatorIndex > 0 && separatorIndex - beginIndex > 19) {
            length = Integer.parseInt(buf.toString(buf.readerIndex() + 2, 4, StandardCharsets.US_ASCII));
        } else {
            length = Integer.parseInt(buf.toString(buf.readerIndex() + 2, 2, StandardCharsets.US_ASCII), 16);
        }

        if (length <= buf.readableBytes()) {
            return buf.readBytes(length);
        }

        return null;
    }
