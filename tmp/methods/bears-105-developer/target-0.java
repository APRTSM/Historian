    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
        org.traccar.helper.PatternUtil.MatchResult matchResult =
                org.traccar.helper.PatternUtil.checkPattern(PATTERN.pattern(), (String) msg);

        Parser parser = new Parser(PATTERN, (String) msg);
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

        position.set(Position.KEY_ALARM, decodeAlarm(event));

        if (event == 11) {
            position.set(Position.KEY_IGNITION, true);
        } else if (event == 12) {
            position.set(Position.KEY_IGNITION, false);
        }

        position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));

        if (parser.hasNext(6)) {
            position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_MIN_MIN));
            position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_MIN_MIN));
        }

        if (parser.hasNext(2)) {
            position.setLongitude(parser.nextDouble() / 10000);
            position.setLatitude(parser.nextDouble() / 10000);
        }

        position.setValid(parser.nextInt() != 1);
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble()));
        position.setCourse(parser.nextDouble());

        if (parser.hasNext()) {
            position.set(Position.KEY_SATELLITES, parser.nextInt());
        }

        position.set(Position.KEY_BATTERY, parser.nextInt());

        if (parser.hasNext()) {
            position.set(Position.KEY_POWER, parser.nextInt());
        }

        return position;
    }
