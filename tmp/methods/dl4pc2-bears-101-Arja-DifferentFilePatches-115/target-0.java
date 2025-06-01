    private Object decodeWif(Channel channel, SocketAddress remoteAddress, String sentence) {
        Parser parser = new Parser(PATTERN_WIF, sentence);
        if (!parser.matches()) {
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        Position position = new Position();
        position.setProtocol(getProtocolName());
        getLastLocation(position, null);

        Network network = new Network();

        int count = parser.nextInt();
        return position;
    }
