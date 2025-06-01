    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = (String) msg;

        // Send response #1
        if (sentence.contains("##")) {
            if (channel != null) {
                channel.write("LOAD", remoteAddress);
                Parser handshakeParser = new Parser(PATTERN_HANDSHAKE, sentence);
                if (handshakeParser.matches()) {
                    getDeviceSession(channel, remoteAddress, handshakeParser.next());
                }
            }
            return null;
        }

        // Send response #2
        if (!sentence.isEmpty() && Character.isDigit(sentence.charAt(0))) {
            if (channel != null) {
                channel.write("ON", remoteAddress);
            }
            int start = sentence.indexOf("imei:");
            if (start >= 0) {
                sentence = sentence.substring(start);
            } else {
                return null;
            }
        }

        Position position = new Position();
        position.setProtocol(getProtocolName());

        Parser parser = new Parser(PATTERN_NETWORK, sentence);
        if (parser.matches()) {

            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
            if (deviceSession == null) {
                return null;
            }
            position.setDeviceId(deviceSession.getDeviceId());

            getLastLocation(position, null);

            position.setNetwork(new Network(
                    CellTower.fromLacCid(parser.nextHexInt(0), parser.nextHexInt(0))));

            return position;

        }

        parser = new Parser(PATTERN_OBD, sentence);
        if (parser.matches()) {

            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
            if (deviceSession == null) {
                return null;
            }
            position.setDeviceId(deviceSession.getDeviceId());

            getLastLocation(position, parser.nextDateTime());

            position.set(Position.KEY_ODOMETER, parser.nextInt(0));
            parser.nextDouble(0); // instant fuel consumption
            position.set(Position.KEY_FUEL_CONSUMPTION, parser.nextDouble(0));
            position.set(Position.KEY_HOURS, parser.nextInt(0));
            position.set(Position.KEY_OBD_SPEED, parser.nextInt(0));
            position.set(Position.PREFIX_TEMP + 1, parser.nextInt(0));
            position.set(Position.KEY_THROTTLE, parser.next());
            position.set(Position.KEY_RPM, parser.nextInt(0));
            position.set(Position.KEY_BATTERY, parser.nextDouble(0));
            position.set(Position.KEY_DTCS, parser.next().replace(',', ' ').trim());

            return position;

        }

        parser = new Parser(PATTERN, sentence);
        if (!parser.matches()) {
            return null;
        }

        String imei = parser.next();
        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, imei);
        if (deviceSession == null) {
            return null;
        }
        position.setDeviceId(deviceSession.getDeviceId());

        String alarm = parser.next();
        position.set(Position.KEY_ALARM, decodeAlarm(alarm));
        if (alarm.equals("help me")) {
            if (channel != null) {
                channel.write("**,imei:" + imei + ",E;", remoteAddress);
            }
        } else if (alarm.equals("acc on")) {
            position.set(Position.KEY_IGNITION, true);
        } else if (alarm.equals("acc off")) {
            position.set(Position.KEY_IGNITION, false);
        } else if (alarm.startsWith("T:")) {
            position.set(Position.PREFIX_TEMP + 1, alarm.substring(2));
        } else if (alarm.startsWith("oil ")) {
            position.set("oil", alarm.substring(4));
        }

        DateBuilder dateBuilder = new DateBuilder()
                .setDate(parser.nextInt(0), parser.nextInt(0), parser.nextInt(0));

        int localHours = parser.nextInt(0);
        int localMinutes = parser.nextInt(0);

        String rfid = parser.next();
        if (alarm.equals("rfid")) {
            position.set(Position.KEY_RFID, rfid);
        }

        String utcHours = parser.next();
        String utcMinutes = parser.next();

        dateBuilder.setTime(localHours, localMinutes, parser.nextInt(0));

        // Timezone calculation
        if (utcHours != null && utcMinutes != null) {
            int deltaMinutes = (localHours - Integer.parseInt(utcHours)) * 60;
            deltaMinutes += localMinutes - Integer.parseInt(utcMinutes);
            if (deltaMinutes <= -12 * 60) {
                deltaMinutes += 24 * 60;
            } else if (deltaMinutes > 12 * 60) {
                deltaMinutes -= 24 * 60;
            }
            dateBuilder.addMinute(-deltaMinutes);
        }
        position.setTime(dateBuilder.getDate());

        position.setValid(parser.next().equals("A"));
        position.setLatitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN_HEM));
        position.setLongitude(parser.nextCoordinate(Parser.CoordinateFormat.HEM_DEG_MIN_HEM));
        position.setSpeed(parser.nextDouble(0));
        position.setCourse(parser.nextDouble(0));
        position.setAltitude(parser.nextDouble(0));

        for (int i = 1; i <= 5; i++) {
            position.set(Position.PREFIX_IO + i, parser.next());
        }

        return position;
    }
