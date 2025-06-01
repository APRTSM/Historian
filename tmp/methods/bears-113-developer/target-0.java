    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        Position position = new Position();
        position.setProtocol(getProtocolName());

        String type = parser.next();

        if (type.startsWith("PHO") && channel != null) {
            photo = new byte[Integer.parseInt(type.substring(3))];
            channel.write("#PHD0," + Math.min(photo.length, MAX_CHUNK_SIZE) + "\r\n");
        }

        position.set(Position.KEY_ALARM, decodeAlarm(type));

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

        position.set(Position.KEY_INPUT, parser.next());
        position.set(Position.KEY_OUTPUT, parser.next());

        if (parser.hasNext()) {
            String[] values = parser.next().split(",");
            for (int i = 0; i < values.length; i++) {
                position.set(Position.PREFIX_ADC + (i + 1), Integer.parseInt(values[i], 16));
            }
        }

        position.set(Position.KEY_ODOMETER, parser.nextInt(0));
        position.set(Position.KEY_RFID, parser.next());

        if (parser.hasNext()) {
            int value = parser.nextHexInt(0);
            position.set(Position.KEY_BATTERY, value >> 8);
            position.set(Position.KEY_RSSI, (value >> 4) & 0xf);
            position.set(Position.KEY_SATELLITES, value & 0xf);
        }

        return position;
    }
