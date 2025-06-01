    private Position decodeOld(DeviceSession deviceSession, ChannelBuffer buf, int type, int index) {

        Position position = new Position();
        position.setDeviceId(deviceSession.getDeviceId());
        position.setProtocol(getProtocolName());

        position.set(Position.KEY_INDEX, index);

        position.setTime(new Date(buf.readUnsignedInt() * 1000));
        position.setLatitude(buf.readInt() / 1800000.0);
        position.setLongitude(buf.readInt() / 1800000.0);
        position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));
        position.setCourse(buf.readUnsignedShort());

        position.setNetwork(new Network(CellTower.from(
                buf.readUnsignedShort(), buf.readUnsignedShort(), buf.readUnsignedShort(), buf.readUnsignedMedium())));

        if (type == MSG_GPS) {

            if (buf.readableBytes() >= 2) {
                decodeStatus(position, buf.readUnsignedShort());
            }

            if (buf.readableBytes() >= 2 * 4) {

                position.set(Position.KEY_BATTERY, buf.readUnsignedShort() * 0.001);

                position.set(Position.KEY_RSSI, buf.readUnsignedShort());

                position.set(Position.PREFIX_ADC + 1, buf.readUnsignedShort());
                position.set(Position.PREFIX_ADC + 2, buf.readUnsignedShort());

            }

        } else if (type == MSG_ALARM) {

            position.set(Position.KEY_ALARM, decodeAlarm(buf.readUnsignedByte()));

        } else if (type == MSG_STATE) {

            int statusType = buf.readUnsignedByte();

            position.set(Position.KEY_EVENT, statusType);

            if (statusType == 0x01 || statusType == 0x02 || statusType == 0x03) {
                buf.readUnsignedInt(); // device time
            }

        }

        return position;
    }
    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ChannelBuffer buf = (ChannelBuffer) msg;

        buf.skipBytes(2); // header
        int type = buf.readUnsignedByte();
        buf.readShort(); // length
        int index = buf.readUnsignedShort();

        if (type != MSG_GPS && type != MSG_DATA) {
        }

        if (type == MSG_LOGIN) {

            getDeviceSession(channel, remoteAddress, ChannelBuffers.hexDump(buf.readBytes(8)).substring(1));

        } else {
            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
            if (deviceSession == null) {
                return null;
            }

            if (type == MSG_GPS || type == MSG_ALARM || type == MSG_STATE || type == MSG_SMS) {
                return decodeOld(deviceSession, buf, type, index);
            } else if (type >= MSG_NORMAL && type <= MSG_OBD_CODE) {
                return decodeNew(deviceSession, buf, index);
            } else if (type == MSG_HEARTBEAT && buf.readableBytes() >= 2) {

                Position position = new Position();
                position.setDeviceId(deviceSession.getDeviceId());
                position.setProtocol(getProtocolName());

                getLastLocation(position, null);

                decodeStatus(position, buf.readUnsignedShort());

                return position;

            }
        }

        return null;
    }
