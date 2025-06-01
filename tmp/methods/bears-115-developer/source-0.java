    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        String manufacturer = parser.next();
        String id = parser.next();
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, id);
        if (deviceSession == null) {
            return null;
        }

        String type = parser.next();
        String content = parser.next();

        if (type.equals("LK")) {

            sendResponse(channel, manufacturer, id, "LK");

            if (!content.isEmpty()) {
                String[] values = content.split(",");
                if (values.length >= 4) {
                    Position position = new Position();
                    position.setProtocol(getProtocolName());
                    position.setDeviceId(deviceSession.getDeviceId());

                    getLastLocation(position, null);

                    position.set(Position.KEY_BATTERY_LEVEL, Integer.parseInt(values[3]));

                    return position;
                }
            }

        } else if (type.equals("UD") || type.equals("UD2") || type.equals("UD3")
                || type.equals("AL") || type.equals("WT")) {

            if (type.equals("AL")) {
                sendResponse(channel, manufacturer, id, "AL");
            }

            parser = new Parser(PATTERN_POSITION, content);
            if (!parser.matches()) {
                return null;
            }

            Position position = new Position();
            position.setProtocol(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            position.setTime(parser.nextDateTime(Parser.DateTimeFormat.DMY_HMS));

            position.setValid(parser.next().equals("A"));
            position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
            position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.DEG_HEM));
            position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0)));
            position.setCourse(parser.nextDouble(0));
            position.setAltitude(parser.nextDouble(0));

            position.set(Position.KEY_SATELLITES, parser.nextInt(0));
            position.set(Position.KEY_RSSI, parser.nextInt(0));
            position.set(Position.KEY_BATTERY_LEVEL, parser.nextInt(0));

            position.set("steps", parser.nextInt(0));

            position.set(Position.KEY_ALARM, decodeAlarm(parser.nextHexInt(0)));

            decodeTail(position, parser.next());

            return position;

        } else if (type.equals("TKQ")) {

            sendResponse(channel, manufacturer, id, "TKQ");

        } else if (type.equals("PULSE") || type.equals("heart")) {

            Position position = new Position();
            position.setProtocol(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());
            getLastLocation(position, new Date());
            position.setValid(false);
            String pulse = content.substring(1);
            position.set("pulse", pulse);
            position.set(Position.KEY_RESULT, pulse);
            return position;

        }

        return null;
    }
