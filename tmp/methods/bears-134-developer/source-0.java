    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        String type = parser.next();

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position();
        position.setProtocol(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        switch (type) {
            case "rmv":
                position.set(Position.KEY_ALARM, Position.ALARM_POWER_CUT);
                break;
            case "ebl":
                position.set(Position.KEY_ALARM, Position.ALARM_LOW_POWER);
                break;
            case "ibl":
                position.set(Position.KEY_ALARM, Position.ALARM_LOW_BATTERY);
                break;
            case "tmp":
            case "smt":
            case "btt":
                position.set(Position.KEY_ALARM, Position.ALARM_TAMPERING);
                break;
            case "ion":
                position.set(Position.KEY_IGNITION, true);
                break;
            case "iof":
                position.set(Position.KEY_IGNITION, false);
                break;
            default:
                break;
        }

        position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));

        position.setValid(parser.nextInt() > 0);
        position.setLatitude(parser.nextCoordinate());
        position.setLongitude(parser.nextCoordinate());
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble()));
        position.setCourse(parser.nextDouble());

        if (parser.hasNext(15)) {

            position.setAltitude(parser.nextDouble());

            position.set(Position.KEY_HDOP, parser.nextDouble());
            position.set(Position.KEY_SATELLITES, parser.nextInt());
            position.set(Position.KEY_SATELLITES_VISIBLE, parser.nextInt());
            position.set(Position.KEY_OPERATOR, parser.next());
            position.set(Position.KEY_RSSI, parser.nextInt());
            position.set(Position.KEY_IGNITION, parser.nextInt() == 1);
            position.set(Position.KEY_BATTERY, parser.nextDouble());
            position.set(Position.KEY_POWER, parser.nextDouble());

            int input = parser.nextBinInt();
            int output = parser.nextBinInt();

            if (!BitUtil.check(input, 0)) {
                position.set(Position.KEY_ALARM, Position.ALARM_SOS);
            }

            position.set(Position.KEY_INPUT, input);
            position.set(Position.KEY_OUTPUT, output);

            position.set(Position.PREFIX_ADC + 1, parser.nextDouble());
            position.set(Position.PREFIX_ADC + 2, parser.nextDouble());
            position.set(Position.KEY_VERSION_FW, parser.next());
            position.set(Position.KEY_DRIVER_UNIQUE_ID, parser.next());

        }

        if (parser.hasNext()) {

            position.set(Position.KEY_RSSI, parser.nextInt());
            position.set(Position.KEY_SATELLITES, parser.nextInt());
            position.set(Position.KEY_BATTERY_LEVEL, parser.nextInt());
            position.set(Position.KEY_IGNITION, parser.nextInt() == 1);
            position.set(Position.PREFIX_ADC + 1, parser.nextDouble());
            position.set(Position.PREFIX_ADC + 2, parser.nextDouble());
            position.set(Position.KEY_ODOMETER, parser.nextInt());

        }

        return position;
    }
