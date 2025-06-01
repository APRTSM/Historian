    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        Parser parser = new Parser(PATTERN, (String) msg);
        if (!parser.matches()) {
            return null;
        }

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, parser.next());
        if (deviceSession == null) {
            return null;
        }

        int type = parser.nextInt();
        if (type != MSG_EVENT_REPORT) {
            return null;
        }

        Position position = new Position();
        position.setProtocol(getProtocolName());
        position.setDeviceId(deviceSession.getDeviceId());
        position.setValid(true);

        position.set(Position.KEY_INDEX, parser.nextInt());

        String[] data = parser.next().split(",");
        Integer lac = null, cid = null;
        int event = 0;

        for (int i = 0; i < Math.min(data.length, dataTags.length); i++) {
            switch (dataTags[i]) {
                case "#EDT#":
                    position.setDeviceTime(dateFormat.parse(data[i]));
                    break;
                case "#EID#":
                    event = Integer.parseInt(data[i]);
                    position.set(Position.KEY_EVENT, event);
                    break;
                case "#PDT#":
                    position.setFixTime(dateFormat.parse(data[i]));
                    break;
                case "#LAT#":
                    position.setLatitude(parseCoordinate(data[i]));
                    break;
                case "#LONG#":
                    position.setLongitude(parseCoordinate(data[i]));
                    break;
                case "#SPD#":
                    position.setSpeed(Double.parseDouble(data[i]));
                    break;
                case "#HEAD#":
                    position.setCourse(Integer.parseInt(data[i]));
                    break;
                case "#ODO#":
                    position.set(Position.KEY_ODOMETER, Long.parseLong(data[i]) * 1000);
                    break;
                case "#IN1#":
                    position.set(Position.PREFIX_IN + 1, Integer.parseInt(data[i]));
                    break;
                case "#IN2#":
                    position.set(Position.PREFIX_IN + 2, Integer.parseInt(data[i]));
                    break;
                case "#IN3#":
                    position.set(Position.PREFIX_IN + 3, Integer.parseInt(data[i]));
                    break;
                case "#IN4#":
                    position.set(Position.PREFIX_IN + 4, Integer.parseInt(data[i]));
                    break;
                case "#OUT1#":
                    position.set(Position.PREFIX_OUT + 1, Integer.parseInt(data[i]));
                    break;
                case "#OUT2#":
                    position.set(Position.PREFIX_OUT + 2, Integer.parseInt(data[i]));
                    break;
                case "#OUT3#":
                    position.set(Position.PREFIX_OUT + 3, Integer.parseInt(data[i]));
                    break;
                case "#OUT4#":
                    position.set(Position.PREFIX_OUT + 4, Integer.parseInt(data[i]));
                    break;
                case "#LAC#":
                    if (!data[i].isEmpty()) {
                        lac = Integer.parseInt(data[i]);
                    }
                    break;
                case "#CID#":
                    if (!data[i].isEmpty()) {
                        cid = Integer.parseInt(data[i]);
                    }
                    break;
                case "#VIN#":
                    position.set(Position.KEY_POWER, Double.parseDouble(data[i]));
                    break;
                case "#VBAT#":
                    position.set(Position.KEY_BATTERY, Double.parseDouble(data[i]));
                    break;
                case "#DEST#":
                    position.set("destination", data[i]);
                    break;
                case "#IGN#":
                    position.set(Position.KEY_IGNITION, data[i].equals("1"));
                    break;
                case "#ENG#":
                    position.set("engine", data[i].equals("1"));
                    break;
                default:
                    break;
            }
        }

        if (lac != null && cid != null) {
            position.setNetwork(new Network(CellTower.fromLacCid(lac, cid)));
        }

        if (event == 20) {
            position.set(Position.KEY_RFID, data[data.length - 1]);
        }

        return position;
    }
