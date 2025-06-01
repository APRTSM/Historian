    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ChannelBuffer buf = (ChannelBuffer) msg;

        buf.readUnsignedByte(); // start marker
        buf.readUnsignedByte(); // type

        String sentence = buf.readBytes(buf.readableBytes() - 2).toString(StandardCharsets.US_ASCII);

        Parser parser = new Parser(PATTERN, sentence);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position();
        position.setProtocol(getProtocolName());

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }
        position.setDeviceId(deviceSession.getDeviceId());

        DateBuilder dateBuilder = new DateBuilder()
                .setTime(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));

        position.setValid(parser.next().equals("A"));
        position.setLatitude(parser.nextCoordinate());
        position.setLongitude(parser.nextCoordinate());
        position.setSpeed(parser.nextDouble(0));
        position.setCourse(parser.nextDouble(0));

        dateBuilder.setDateReverse(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));
        position.setTime(dateBuilder.getDate());

        position.set(Position.KEY_STATUS, parser.next());
        position.set(Position.PREFIX_ADC + 1, parser.next());
        position.set(Position.KEY_ODOMETER, parser.nextDouble(0));
        position.set(Position.PREFIX_TEMP + 1, parser.nextDouble(0));
        position.set(Position.KEY_BATTERY, parser.nextDouble(0));

        int rssi = parser.nextInt(0);
        position.setNetwork(new Network(CellTower.from(
                parser.nextInt(0), parser.nextInt(0), parser.nextInt(0), parser.nextInt(0), rssi)));

        return position;
    }
