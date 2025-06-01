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
    private void sendResponse(Channel channel, boolean extended, int type) {
        if (channel != null) {
            ChannelBuffer response = ChannelBuffers.dynamicBuffer();
            if (extended) {
                response.writeShort(0x7979);
                response.writeShort(5);
            } else {
                response.writeShort(0x7878);
                response.writeByte(5);
            }
            response.writeByte(type);
            response.writeShort(++serverIndex);
            response.writeShort(Checksum.crc16(Checksum.CRC16_X25,
                    response.toByteBuffer(2, response.writerIndex() - 2)));
            response.writeByte('\r'); response.writeByte('\n'); // ending
            channel.write(response);
        }
    }
