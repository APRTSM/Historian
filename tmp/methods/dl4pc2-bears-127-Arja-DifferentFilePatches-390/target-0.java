    protected Object decode(
            Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {

        ChannelBuffer buf = (ChannelBuffer) msg;

        buf.skipBytes(2); // header
        int type = buf.readUnsignedByte();
        buf.readShort(); // length
        int index = buf.readUnsignedShort();

        if (type != MSG_GPS && type != MSG_DATA) {
            sendResponse(channel, type, index);
        }

        if (type == MSG_LOGIN) {

            getDeviceSession(channel, remoteAddress, ChannelBuffers.hexDump(buf.readBytes(8)).substring(1));

        } else {
            DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
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

        position.setValid((buf.readUnsignedByte() & 0x01) != 0);

        return position;
    }
