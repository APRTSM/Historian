    public void setSignalStrength(Integer signalStrength) {
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

        Network network = new Network();

        int count = parser.nextInt();
        return position;
    }
    public void getLastLocation(Position position, Date deviceTime) {
        if (position.getDeviceId() != 0) {
            position.setOutdated(true);

            Position last = Context.getIdentityManager().getLastPosition(position.getDeviceId());
            if (deviceTime != null) {
                position.setDeviceTime(deviceTime);
            } else {
                position.setDeviceTime(new Date());
            }
        }
    }
