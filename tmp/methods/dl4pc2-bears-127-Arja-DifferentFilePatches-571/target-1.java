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

        } else
			;

        return position;
    }
    public void setSupportedDataCommands(String... commands) {
    }
