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
        double latitude = buf.readUnsignedInt() / 60.0 / 30000.0;
        double longitude = buf.readUnsignedInt() / 60.0 / 30000.0;
        position.setSpeed(UnitsConverter.knotsFromKph(buf.readUnsignedByte()));

        int flags = buf.readUnsignedShort();
        position.setValid(BitUtil.check(flags, 12));

        if (!BitUtil.check(flags, 10)) {
            latitude = -latitude;
        }
        if (BitUtil.check(flags, 11)) {
            longitude = -longitude;
        }

        position.setLatitude(latitude);
        position.setLongitude(longitude);

        return true;
    }
