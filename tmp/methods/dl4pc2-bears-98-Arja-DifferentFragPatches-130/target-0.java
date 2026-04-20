    private Position decodePosition(DeviceSession deviceSession, Parser parser, Date time) {

        Position position = new Position();
        position.setProtocol(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        if (time != null) {
            position.setTime(time);
        }

        position.set(Position.KEY_EVENT, parser.next());

        position.setValid(parser.next().equals("A"));
        position.set(Position.KEY_SATELLITES, parser.next());

        position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG));
        position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG));
        position.setCourse(parser.nextDouble());
        position.set(Position.KEY_HDOP, parser.next());

        if (parser.hasNext()) {
            position.set(Position.KEY_ODOMETER, parser.nextInt());
        }
        position.set(Position.KEY_POWER, parser.next());
        position.set(Position.KEY_BATTERY, parser.next());

        if (parser.hasNext()) {
            position.set("eventData", parser.next());
        }

        if (parser.hasNext()) {
            position.set("obd", parser.next());
        }

        if (parser.hasNext()) {
            position.set("tagData", parser.next());
        }

        return position;
    }
