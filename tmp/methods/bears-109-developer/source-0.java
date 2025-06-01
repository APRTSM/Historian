    private Position decode9(
            Channel channel, SocketAddress remoteAddress, String[] values) throws ParseException {
        int index = 1;

        String type = values[index++];

        if (!type.equals("Location") && !type.equals("Emergency") && !type.equals("Alert")) {
            return null;
        }

        Position position = new Position();
        position.setProtocol(getProtocolName());

        if (type.equals("Emergency") || type.equals("Alert")) {
            position.set(Position.KEY_ALARM, Position.ALARM_GENERAL);
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, values[index++]);
        if (deviceSession == null) {
            return null;
        }
        position.setDeviceId(deviceSession.getDeviceId());

        position.set(Position.KEY_VERSION_FW, values[index++]);

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        position.setTime(dateFormat.parse(values[index++] + values[index++]));

        if (protocolType == 1) {
            index += 1; // cell
        }

        position.setLatitude(Double.parseDouble(values[index++]));
        position.setLongitude(Double.parseDouble(values[index++]));
        position.setSpeed(UnitsConverter.knotsFromKph(Double.parseDouble(values[index++])));
        position.setCourse(Double.parseDouble(values[index++]));

        position.setValid(values[index++].equals("1"));

        if (protocolType == 1) {
            position.set(Position.KEY_ODOMETER, Integer.parseInt(values[index]));
        }

        return position;
    }
