    private Object decodeBasicOther(Channel channel, ChannelBuffer buf,
            DeviceSession deviceSession, int type, int dataLength) throws Exception {

        Position position = new Position();
        position.setDeviceId(deviceSession.getDeviceId());
        position.setProtocol(getProtocolName());

        if (type == MSG_LBS_MULTIPLE || type == MSG_LBS_EXTEND || type == MSG_LBS_WIFI
                || type == MSG_LBS_2 || type == MSG_WIFI_3) {

            boolean longFormat = type == MSG_LBS_2 || type == MSG_WIFI_3;

            DateBuilder dateBuilder = new DateBuilder(timeZone)
                    .setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte())
                    .setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());

            getLastLocation(position, dateBuilder.getDate());

            int mcc = buf.readUnsignedShort();
            int mnc = BitUtil.check(mcc, 15) ? buf.readUnsignedShort() : buf.readUnsignedByte();
            Network network = new Network();
            for (int i = 0; i < 7; i++) {
                int lac = longFormat ? buf.readInt() : buf.readUnsignedShort();
                int cid = longFormat ? (int) buf.readLong() : buf.readUnsignedMedium();
                int rssi = -buf.readUnsignedByte();
                if (lac > 0) {
                    network.addCellTower(CellTower.from(BitUtil.to(mcc, 15), mnc, lac, cid, rssi));
                }
            }

            buf.readUnsignedByte(); // time leads

            if (type != MSG_LBS_MULTIPLE && type != MSG_LBS_2) {
                int wifiCount = buf.readUnsignedByte();
                for (int i = 0; i < wifiCount; i++) {
                    String mac = ChannelBuffers.hexDump(buf.readBytes(6)).replaceAll("(..)", "$1:");
                    network.addWifiAccessPoint(WifiAccessPoint.from(
                            mac.substring(0, mac.length() - 1), buf.readUnsignedByte()));
                }
            }

            position.setNetwork(network);

        } else if (type == MSG_STRING) {

            getLastLocation(position, null);

            int commandLength = buf.readUnsignedByte();

            if (commandLength > 0) {
                buf.readUnsignedByte(); // server flag (reserved)
                position.set(Position.KEY_RESULT,
                        buf.readBytes(commandLength - 1).toString(StandardCharsets.US_ASCII));
            }

        } else if (isSupported(type)) {

            if (hasGps(type)) {
                decodeGps(position, buf, false);
            } else {
                getLastLocation(position, null);
            }

            if (hasLbs(type)) {
                decodeLbs(position, buf, hasStatus(type));
            }

            if (hasStatus(type)) {
                decodeStatus(position, buf);
            }

            if (type == MSG_GPS_LBS_1 && buf.readableBytes() >= 4 + 6) {
                position.set(Position.KEY_ODOMETER, buf.readUnsignedInt());
            }

            if (type == MSG_GPS_LBS_2 && buf.readableBytes() >= 3 + 6) {
                position.set(Position.KEY_IGNITION, buf.readUnsignedByte() > 0);
                position.set(Position.KEY_EVENT, buf.readUnsignedByte()); // reason
                position.set(Position.KEY_ARCHIVE, buf.readUnsignedByte() > 0);
            }

        } else {

            buf.skipBytes(dataLength);
            if (type != MSG_COMMAND_0 && type != MSG_COMMAND_1 && type != MSG_COMMAND_2) {
                sendResponse(channel, false, type, null);
            }
            return null;

        }

        if (hasLanguage(type)) {
            buf.readUnsignedShort();
        }

        if (type == MSG_GPS_LBS_STATUS_3) {
            position.set(Position.KEY_GEOFENCE, buf.readUnsignedByte());
        }

        sendResponse(channel, false, type, null);

        return position;
    }
