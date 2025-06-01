    private boolean filterInvalid(Position position) {
        return filterInvalid && (!position.getValid()
           || position.getLatitude() > 90 || position.getLongitude() > 180
           || position.getLatitude() < -90 || position.getLongitude() < -180);
    }
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ChannelBuffer buf = (ChannelBuffer) msg;

        if (buf.getUnsignedByte(0) == 0xD0) {

            long deviceId = ((Long.reverseBytes(buf.getLong(0))) >> 32) & 0xFFFFFFFFL;
            getDeviceSession(channel, remoteAddress, String.valueOf(deviceId));

            return null;
        } else if (buf.toString(StandardCharsets.US_ASCII).startsWith("$OK:")
                || buf.toString(StandardCharsets.US_ASCII).startsWith("$ERR:")
                  || buf.toString(StandardCharsets.US_ASCII).startsWith("$MSG:")) {

            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);

            Position position = new Position();
            position.setProtocol(getProtocolName());
            position.setDeviceId(deviceSession.getDeviceId());
            getLastLocation(position, new Date());
            position.set(Position.KEY_RESULT, buf.toString(StandardCharsets.US_ASCII));

            return position;
        } else {

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

            position.setTime(parser.nextDateTime());

            position.setLongitude(parser.nextDouble(0));
            position.setLatitude(parser.nextDouble(0));
            position.setSpeed(UnitsConverter.knotsFromKph(parser.nextDouble(0)));
            position.setCourse(parser.nextDouble(0));
            position.setAltitude(parser.nextDouble(0));

            position.setValid(true);
            position.set(Position.KEY_SATELLITES, parser.nextInt(0));

            position.set(Position.KEY_EVENT, parser.next());
            position.set(Position.KEY_BATTERY, parser.nextDouble());
            if (parser.hasNext()) {
                position.set(Position.KEY_ODOMETER, parser.nextDouble(0) * 1000);
            }
            position.set(Position.KEY_INPUT, parser.next());
            position.set(Position.PREFIX_ADC + 1, parser.next());
            position.set(Position.PREFIX_ADC + 2, parser.next());
            position.set(Position.KEY_OUTPUT, parser.next());

            return position;
        }

    }
