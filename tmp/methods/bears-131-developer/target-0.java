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

        int event = parser.nextInt(0);
        position.set(Position.KEY_EVENT, event);
        position.set(Position.KEY_ALARM, decodeAlarm(event));

        position.setLatitude(parser.nextDouble(0));
        position.setLongitude(parser.nextDouble(0));

        position.setTime(parser.nextDateTime());

        position.setValid(parser.next().equals("A"));

        position.set(Position.KEY_SATELLITES, parser.nextInt());
        int rssi = parser.nextInt(0);

        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0)));
        position.setCourse(parser.nextDouble(0));

        position.set(Position.KEY_HDOP, parser.nextDouble());

        position.setAltitude(parser.nextDouble(0));

        position.set(Position.KEY_ODOMETER, parser.nextInt(0));
        position.set("runtime", parser.next());

        position.setNetwork(new Network(CellTower.from(
                parser.nextInt(0), parser.nextInt(0), parser.nextHexInt(0), parser.nextHexInt(0), rssi)));

        position.set(Position.KEY_STATUS, parser.next());

        for (int i = 1; i <= 3; i++) {
            if (parser.hasNext()) {
                position.set(Position.PREFIX_ADC + i, parser.nextHexInt(0));
            }
        }

        String deviceModel = Context.getIdentityManager().getById(deviceSession.getDeviceId()).getModel();
        if (deviceModel == null) {
            deviceModel = "";
        }
        switch (deviceModel.toUpperCase()) {
            case "MVT340":
            case "MVT380":
                position.set(Position.KEY_BATTERY, parser.nextHexInt(0) * 3.0 * 2.0 / 1024.0);
                position.set(Position.KEY_POWER, parser.nextHexInt(0) * 3.0 * 16.0 / 1024.0);
                break;
            case "MT90":
                position.set(Position.KEY_BATTERY, parser.nextHexInt(0) * 3.3 * 2.0 / 4096.0);
                position.set(Position.KEY_POWER, parser.nextHexInt(0));
                break;
            case "T1":
            case "T3":
            case "MVT100":
            case "MVT600":
            case "MVT800":
            case "TC68":
            case "TC68S":
                position.set(Position.KEY_BATTERY, parser.nextHexInt(0) * 3.3 * 2.0 / 4096.0);
                position.set(Position.KEY_POWER, parser.nextHexInt(0) * 3.3 * 16.0 / 4096.0);
                break;
            case "T311":
            case "T322X":
            case "T333":
            case "T355":
                position.set(Position.KEY_BATTERY, parser.nextHexInt(0) / 100.0);
                position.set(Position.KEY_POWER, parser.nextHexInt(0) / 100.0);
                break;
            default:
                position.set(Position.KEY_BATTERY, parser.nextHexInt(0));
                position.set(Position.KEY_POWER, parser.nextHexInt(0));
                break;
        }

        String eventData = parser.next();
        if (eventData != null && !eventData.isEmpty()) {
            switch (event) {
                case 37:
                    position.set(Position.KEY_DRIVER_UNIQUE_ID, eventData);
                    break;
                default:
                    position.set("eventData", eventData);
                    break;
            }
        }

        int protocol = parser.nextInt(0);

        if (parser.hasNext()) {
            String fuel = parser.next();
            position.set(Position.KEY_FUEL_LEVEL,
                    Integer.parseInt(fuel.substring(0, 2), 16) + Integer.parseInt(fuel.substring(2), 16) * 0.01);
        }

        if (parser.hasNext()) {
            for (String temp : parser.next().split("\\|")) {
                int index = Integer.parseInt(temp.substring(0, 2), 16);
                if (protocol >= 3) {
                    double value = (short) Integer.parseInt(temp.substring(2), 16);
                    position.set(Position.PREFIX_TEMP + index, value * 0.01);
                } else {
                    double value = Byte.parseByte(temp.substring(2, 4), 16);
                    value += (value < 0 ? -0.01 : 0.01) * Integer.parseInt(temp.substring(4), 16);
                    position.set(Position.PREFIX_TEMP + index, value);
                }
            }
        }

        return position;
    }
