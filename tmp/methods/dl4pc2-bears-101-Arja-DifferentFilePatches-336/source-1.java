    public void setSignalStrength(Integer signalStrength) {
        this.signalStrength = signalStrength;
    }
    private Object decodeWif(Channel channel, SocketAddress remoteAddress, String sentence) {
        Parser parser = new Parser(PATTERN_WIF, sentence);
        if (!parser.matches()) {
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position();
        position.setProtocol(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        getLastLocation(position, null);

        Network network = new Network();

        int count = parser.nextInt();
        for (int i = 0; i < count; i++) {
            String mac = parser.next().replaceAll("(..)", "$1:");
            network.addWifiAccessPoint(WifiAccessPoint.from(mac.substring(0, mac.length() - 1), parser.nextInt()));
        }

        return position;
    }
