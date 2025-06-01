    private boolean decodeGps(Position position, ChannelBuffer buf, boolean hasLength) {

        DateBuilder dateBuilder = new DateBuilder(timeZone)
                .setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte())
                .setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());
        position.setTime(dateBuilder.getDate());

        if (hasLength && buf.readUnsignedByte() == 0) {
            return false;
        }

        int length = buf.readUnsignedByte();
        position.set(Position.KEY_SATELLITES, BitUtil.to(length, 4));
        length = BitUtil.from(length, 4);

        double latitude = buf.readUnsignedInt() / 60.0 / 30000.0;
        double longitude = buf.readUnsignedInt() / 60.0 / 30000.0;
        position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));

        int flags = buf.readUnsignedShort();
        position.setCourse(BitUtil.to(flags, 10));
        position.setValid(BitUtil.check(flags, 12));

        if (!BitUtil.check(flags, 10)) {
            latitude = -latitude;
        }
        if (BitUtil.check(flags, 11)) {
            longitude = -longitude;
        }

        position.setLatitude(latitude);
        position.setLongitude(longitude);

        if (BitUtil.check(flags, 14)) {
            position.set(Position.KEY_IGNITION, BitUtil.check(flags, 15));
        }

        if (length > 0) {
            buf.skipBytes(length - 12); // skip reserved
        }

        return true;
    }
    private Object decodeBasicOther(Channel channel, ChannelBuffer buf,
            DeviceSession deviceSession, int type, int dataLength) throws Exception {

        Position position = new Position();
        position.setDeviceId(deviceSession.getDeviceId());
        position.setProtocol(getProtocolName());

        if (type == MSG_LBS_MULTIPLE || type == MSG_LBS_EXTEND || type == MSG_LBS_WIFI) {

            DateBuilder dateBuilder = new DateBuilder(timeZone)
                    .setDate(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte())
                    .setTime(buf.readUnsignedByte(), buf.readUnsignedByte(), buf.readUnsignedByte());

            getLastLocation(position, dateBuilder.getDate());

            int mcc = buf.readUnsignedShort();
            int mnc = buf.readUnsignedByte();
            Network network = new Network();
            for (int i = 0; i < 7; i++) {
                int lac = buf.readUnsignedShort();
                int cid = buf.readUnsignedMedium();
                int rssi = -buf.readUnsignedByte();
                if (lac > 0) {
                    network.addCellTower(CellTower.from(mcc, mnc, lac, cid, rssi));
                }
            }

            buf.readUnsignedByte(); // time leads

            if (type != MSG_LBS_MULTIPLE) {
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
                sendResponse(channel, false, type);
            }
            return null;

        }

        sendResponse(channel, false, type);

        return position;
    }
    private boolean decodeLbs(Position position, ChannelBuffer buf, boolean hasLength) {

        int length = 0;
        if (hasLength) {
            length = buf.readUnsignedByte();
            if (length == 0) {
                return false;
            }
        }

        position.setNetwork(new Network(CellTower.from(
                buf.readUnsignedShort(), buf.readUnsignedByte(), buf.readUnsignedShort(), buf.readUnsignedMedium())));

        if (length > 0) {
            buf.skipBytes(length - 8);
        }

        return true;
    }
    private Object decodeExtended(Channel channel, SocketAddress remoteAddress, ChannelBuffer buf) throws Exception {

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);
        if (deviceSession == null) {
            return null;
        }

        Position position = new Position();
        position.setDeviceId(deviceSession.getDeviceId());
        position.setProtocol(getProtocolName());

        buf.readUnsignedShort(); // length
        int type = buf.readUnsignedByte();

        if (type == MSG_STRING_INFO) {

            buf.readUnsignedInt(); // server flag
            String data;
            if (buf.readUnsignedByte() == 1) {
                data = buf.readBytes(buf.readableBytes() - 6).toString(StandardCharsets.US_ASCII);
            } else {
                data = buf.readBytes(buf.readableBytes() - 6).toString(StandardCharsets.UTF_16BE);
            }

            if (decodeLocationString(position, data) == null) {
                getLastLocation(position, null);
                position.set(Position.KEY_RESULT, data);
            }

            return position;

        } else if (type == MSG_INFO) {

            int subType = buf.readUnsignedByte();

            getLastLocation(position, null);

            if (subType == 0x00) {

                position.set(Position.KEY_POWER, buf.readUnsignedShort() * 0.01);
                return position;

            } else if (subType == 0x05) {

                int flags = buf.readUnsignedByte();
                position.set(Position.KEY_DOOR, BitUtil.check(flags, 0));
                position.set(Position.PREFIX_IO + 1, BitUtil.check(flags, 2));
                return position;

            } else if (subType == 0x0d) {

                buf.skipBytes(6);
                return decodeFuelData(position, buf.toString(
                        buf.readerIndex(), buf.readableBytes() - 4 - 2, StandardCharsets.US_ASCII));

            }

        } else if (type == MSG_X1_PHOTO_DATA) {

            int pictureId = buf.readInt();

            ChannelBuffer photo = photos.get(pictureId);

            buf.readUnsignedInt(); // offset
            buf.readBytes(photo, buf.readUnsignedShort());

            if (photo.writableBytes() > 0) {
                sendPhotoRequest(channel, pictureId);
            } else {
                Device device = Context.getDeviceManager().getById(deviceSession.getDeviceId());
                Context.getMediaManager().writeFile(device.getUniqueId(), photo, "jpg");
                photos.remove(pictureId);
            }

        } else if (type == MSG_AZ735_GPS || type == MSG_AZ735_ALARM) {

            if (!decodeGps(position, buf, true)) {
                getLastLocation(position, position.getDeviceTime());
            }

            decodeLbs(position, buf, true);

            buf.skipBytes(buf.readUnsignedByte()); // additional cell towers
            buf.skipBytes(buf.readUnsignedByte()); // wifi access point

            int status = buf.readUnsignedByte();
            position.set(Position.KEY_STATUS, status);

            if (type == MSG_AZ735_ALARM) {
                switch (status) {
                    case 0xA0:
                        position.set(Position.KEY_ARMED, true);
                        break;
                    case 0xA1:
                        position.set(Position.KEY_ARMED, false);
                        break;
                    case 0xA2:
                    case 0xA3:
                        position.set(Position.KEY_ALARM, Position.ALARM_LOW_BATTERY);
                        break;
                    case 0xA4:
                        position.set(Position.KEY_ALARM, Position.ALARM_GENERAL);
                        break;
                    case 0xA5:
                        position.set(Position.KEY_ALARM, Position.ALARM_DOOR);
                        break;
                    default:
                        break;
                }
            }

            buf.skipBytes(buf.readUnsignedByte()); // reserved extension

            sendResponse(channel, true, type);

            return position;

        }

        return null;
    }
