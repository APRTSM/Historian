    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        String sentence = (String) msg;

        int typeIndex = sentence.indexOf(":GT");
        if (typeIndex < 0) {
            return null;
        }

        Object result;
        String type = sentence.substring(typeIndex + 3, typeIndex + 6);
        if (sentence.startsWith("+ACK")) {
            result = decodeAck(channel, remoteAddress, sentence, type);
        } else {
            switch (type) {
                case "INF":
                    result = decodeInf(channel, remoteAddress, sentence);
                    break;
                case "OBD":
                    result = decodeObd(channel, remoteAddress, sentence);
                    break;
                case "FRI":
                    result = decodeFri(channel, remoteAddress, sentence);
                    break;
                case "IGN":
                case "IGF":
                    result = decodeIgn(channel, remoteAddress, sentence);
                    break;
                case "IDA":
                    result = decodeIda(channel, remoteAddress, sentence);
                    break;
                case "WIF":
                    result = decodeWif(channel, remoteAddress, sentence);
                    break;
                case "VER":
                    result = decodeVer(channel, remoteAddress, sentence);
                    break;
                default:
                    result = decodeOther(channel, remoteAddress, sentence, type);
                    break;
            }

            if (result == null) {
                result = decodeBasic(channel, remoteAddress, sentence, type);
            }

            if (result != null) {
                if (result instanceof Position) {
                    ((Position) result).set(Position.KEY_TYPE, type);
                } else {
                    for (Position p : (List<Position>) result) {
                        p.set(Position.KEY_TYPE, type);
                    }
                }
            }
        }

        return result;
    }
    private Object decodeWif(Channel channel, SocketAddress remoteAddress, String sentence) {
        Parser parser = new Parser(PATTERN_WIF, sentence);
        if (!parser.matches()) {
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position();
        position.setProtocol(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());

        getLastLocation(position, null);

        Network network = new Network();

        int count = parser.nextInt();
        for (int i = 0; i < count; i++) {
            String mac = parser.next().replaceAll("(..)", "$1:");
            network.addWifiAccessPoint(WifiAccessPoint.from(mac.substring(0, mac.length() - 1), parser.nextInt()));
        }

        return position;
    }
    public static WifiAccessPoint from(String macAddress, int signalStrength) {
        WifiAccessPoint wifiAccessPoint = new WifiAccessPoint();
        wifiAccessPoint.setMacAddress(macAddress);
        wifiAccessPoint.setSignalStrength(signalStrength);
        return wifiAccessPoint;
    }
    public void getLastLocation(Position position, Date deviceTime) {
        if (position.getDeviceId() != 0) {
            position.setOutdated(true);

            Position last = Context.getIdentityManager().getLastPosition(position.getDeviceId());
            if (last != null) {
                position.setFixTime(last.getFixTime());
                position.setValid(last.getValid());
                position.setLatitude(last.getLatitude());
                position.setLongitude(last.getLongitude());
                position.setAltitude(last.getAltitude());
                position.setSpeed(last.getSpeed());
                position.setCourse(last.getCourse());
            } else {
                position.setFixTime(new Date(0));
            }

            if (deviceTime != null) {
                position.setDeviceTime(deviceTime);
            } else {
                position.setDeviceTime(new Date());
            }
        }
    }
