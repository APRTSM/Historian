    private Object decodeEri(Channel channel, SocketAddress remoteAddress, String sentence) {
        Parser parser = new Parser(PATTERN_ERI, sentence);
        if (!parser.matches()) {
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }

        LinkedList<Position> positions = new LinkedList<>();

        int power = parser.nextInt(0);

        Parser itemParser = new Parser(PATTERN_LOCATION, parser.next());
        while (itemParser.find()) {
            Position position = new Position();
            position.setProtocol(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            decodeLocation(position, itemParser);

            positions.add(position);
        }

        Position position = positions.getLast();

        decodeLocation(position, parser);

        position.set(Position.KEY_POWER, power);
        position.set(Position.KEY_ODOMETER, parser.nextDouble(0) * 1000);
        position.set(Position.KEY_HOURS, parser.next());
        position.set(Position.PREFIX_ADC + 1, parser.next());
        position.set(Position.PREFIX_ADC + 2, parser.next());
        position.set(Position.KEY_BATTERY_LEVEL, parser.nextInt());

        decodeStatus(position, parser);

        int index = 0;
        String[] data = parser.next().split(",");
        if (data.length > 1) {
            int deviceType = Integer.parseInt(data[index++]);
            if (deviceType == 2) {
                int deviceCount = Integer.parseInt(data[index++]);
                for (int i = 1; i <= deviceCount; i++) {
                    index++; // id
                    index++; // type
                    position.set(Position.PREFIX_TEMP + i, Short.parseShort(data[index++], 16) * 0.0625);
                }
            }
        }

        decodeDeviceTime(position, parser);

        return positions;
    }
