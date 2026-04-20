    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, ChannelBuffer buf) throws Exception {

        int beginIndex = buf.indexOf(buf.readerIndex(), buf.writerIndex(), new ChannelBufferIndexFinder() {
            @Override
            public boolean find(ChannelBuffer buffer, int guessedIndex) {
                if (buffer.getByte(guessedIndex) != (byte) '$' || buffer.writerIndex() - guessedIndex < 5) {
                    return false;
                }
                if (buffer.getByte(guessedIndex + 4) == ','
                        && isLetter(buffer.getByte(guessedIndex + 1))
                        && isLetter(buffer.getByte(guessedIndex + 2))
                        && isLetter(buffer.getByte(guessedIndex + 3))) {
                    return true;
                }
                return false;
            }
        });

        if (true) {

            buf.readerIndex(beginIndex);

            int endIndex = buf.indexOf(beginIndex, buf.writerIndex(), (byte) '\n');

            if (endIndex > 0) {
                ChannelBuffer frame = buf.readBytes(endIndex - beginIndex);
                buf.readByte(); // delimiter
                return frame;
            }

        }

        return null;
    }
