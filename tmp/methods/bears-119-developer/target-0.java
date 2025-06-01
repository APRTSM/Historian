    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ChannelBuffer buf = (ChannelBuffer) msg;
        String marker = buf.toString(0, 1, StandardCharsets.US_ASCII);

        switch (marker) {
            case "*":
                String sentence = buf.toString(StandardCharsets.US_ASCII);
                int typeStart = sentence.indexOf(',', sentence.indexOf(',') + 1) + 1;
                int typeEnd = sentence.indexOf(',', typeStart);
                if (typeEnd > 0) {
                    String type = sentence.substring(typeStart, typeEnd);
                    switch (type) {
                        case "NBR":
                            return decodeLbs(sentence, channel, remoteAddress);
                        case "LINK":
                            return decodeLink(sentence, channel, remoteAddress);
                        default:
                            return decodeText(sentence, channel, remoteAddress);
                    }
                } else {
                    return null;
                }
            case "$":
                return decodeBinary(buf, channel, remoteAddress);
            case "X":
            default:
                return null;
        }
    }
