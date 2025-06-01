    private Position decodePosition(DeviceSession deviceSession, Parser parser, Date time) {

        Position position = new Position();
        position.setProtocol(getProtocolName());
        if (time != null) {
            position.setTime(time);
        }

        position.set(Position.KEY_EVENT, parser.next());

        position.setValid(parser.next().equals("A"));
        position.set(Position.KEY_SATELLITES, parser.next());

        position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG));
        position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG));
        position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble()));
        position.setCourse(parser.nextDouble());
        position.setAltitude(parser.nextDouble());

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
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        if (channel != null) {
            channel.write("1234");
        }

        String sentence = (String) msg;
        Pattern pattern = PATTERN;
        if (sentence.startsWith("*GS02")) {
            pattern = PATTERN_OLD;
        }

        Parser parser = new Parser(pattern, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (pattern == PATTERN_OLD) {

            Position position = new Position();
            position.setProtocol(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());

            DateBuilder dateBuilder = new DateBuilder()
                    .setTime(parser.nextInt(), parser.nextInt(), parser.nextInt());

            position.setValid(parser.next().equals("A"));
            position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG));
            position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG));
            position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble()));
            position.setCourse(parser.nextDouble());

            position.set(Position.KEY_HDOP, parser.next());

            dateBuilder.setDateReverse(parser.nextInt(), parser.nextInt(), parser.nextInt());
            position.setTime(dateBuilder.getDate());

            return position;

        } else {

            Date time = null;
            if (parser.hasNext(6)) {
                DateBuilder dateBuilder = new DateBuilder()
                        .setTime(parser.nextInt(), parser.nextInt(), parser.nextInt())
                        .setDateReverse(parser.nextInt(), parser.nextInt(), parser.nextInt());
                time = dateBuilder.getDate();
            }

            List<Position> positions = new LinkedList<>();
            Parser itemParser = new Parser(PATTERN_ITEM, parser.next());
            while (itemParser.find()) {
                positions.add(decodePosition(deviceSession, itemParser, time));
            }

            return positions;

        }
    }
