    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
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

        position.set(Position.KEY_EVENT, parser.nextInt());

        position.setLatitude(parser.nextDouble());
        position.setLongitude(parser.nextDouble());

        DateBuilder dateBuilder = new DateBuilder()
                .setDate(parser.nextInt(), parser.nextInt(), parser.nextInt())
                .setTime(parser.nextInt(), parser.nextInt(), parser.nextInt());
        position.setTime(dateBuilder.getDate());

        position.setValid(parser.next().equals("A"));

        position.set(Position.KEY_RSSI, parser.nextInt());

        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble()));

        position.set(Position.KEY_ODOMETER, parser.nextInt());

        if (parser.hasNext(9)) {

            position.set(Position.KEY_FUEL, parser.next());
            position.set(Position.PREFIX_IO + 1, parser.next());
            position.set(Position.KEY_CHARGE, parser.next());
            position.set(Position.PREFIX_IO + 2, parser.next());

            position.set(Position.KEY_IGNITION, parser.nextInt() == 1);

            int course = (parser.nextInt() << 3) + (parser.nextInt() << 2) + (parser.nextInt() << 1) + parser.nextInt();
            if (course > 0 && course <= 8) {
                position.setCourse((course - 1) * 45);
            }

        } else {

            position.setCourse(parser.nextInt());

            position.set(Position.KEY_CHARGE, parser.next());
            position.set(Position.KEY_IGNITION, parser.nextInt() == 1);
            position.set(Position.KEY_POWER, parser.nextInt());
            position.set(Position.KEY_BATTERY, parser.nextInt());

            String obd = parser.next();
            position.set("obd", obd.substring(1, obd.length() - 1));

            String dtcs = parser.next();
            position.set(Position.KEY_DTCS, dtcs.substring(1, dtcs.length() - 1).replace('|', ' '));

        }

        return position;
    }
    private Position decodeRegularMessage(Channel channel, SocketAddress remoteAddress, ChannelBuffer buf) {

        Parser parser = new Parser(PATTERN, buf.toString(StandardCharsets.US_ASCII));
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

        int event = parser.nextInt();
        position.set(Position.KEY_EVENT, event);

        position.setLatitude(parser.nextDouble());
        position.setLongitude(parser.nextDouble());

        DateBuilder dateBuilder = new DateBuilder()
                .setDate(parser.nextInt(), parser.nextInt(), parser.nextInt())
                .setTime(parser.nextInt(), parser.nextInt(), parser.nextInt());
        position.setTime(dateBuilder.getDate());

        position.setValid(parser.next().equals("A"));

        position.set(Position.KEY_SATELLITES, parser.next());
        int rssi = parser.nextInt();

        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble()));
        position.setCourse(parser.nextDouble());

        position.set(Position.KEY_HDOP, parser.next());

        position.setAltitude(parser.nextDouble());

        position.set(Position.KEY_ODOMETER, parser.nextInt());
        position.set("runtime", parser.next());

        position.setNetwork(new Network(
                CellTower.from(parser.nextInt(), parser.nextInt(), parser.nextInt(16), parser.nextInt(16), rssi)));

        position.set(Position.KEY_STATUS, parser.next());

        for (int i = 1; i <= 3; i++) {
            if (parser.hasNext()) {
                position.set(Position.PREFIX_ADC + i, parser.nextInt(16));
            }
        }

        position.set(Position.KEY_BATTERY, parser.nextInt(16));
        position.set(Position.KEY_POWER, parser.nextInt(16));

        String eventData = parser.next();
        if (eventData != null && !eventData.isEmpty()) {
            switch (event) {
                case 37:
                    position.set(Position.KEY_RFID, eventData);
                    break;
                default:
                    position.set("eventData", eventData);
                    break;
            }
        }

        if (parser.hasNext()) {
            String fuel = parser.next();
            position.set(Position.KEY_FUEL,
                    Integer.parseInt(fuel.substring(0, 2), 16) + Integer.parseInt(fuel.substring(2), 16) * 0.01);
        }

        if (parser.hasNext()) {
            for (String temp : parser.next().split("\\|")) {
                int index = Integer.valueOf(temp.substring(0, 2), 16);
                int value = Integer.valueOf(temp.substring(2), 16);
                position.set(Position.PREFIX_TEMP + index, value);
            }
        }

        return position;
    }
