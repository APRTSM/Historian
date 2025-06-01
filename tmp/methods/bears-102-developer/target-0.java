    private void decodeParameter(Position position, int id, ChannelBuffer buf, int length) {
        switch (id) {
            case 1:
            case 2:
            case 3:
            case 4:
                position.set("di" + id, buf.readUnsignedByte());
                break;
            case 9:
                position.set(Position.PREFIX_ADC + 1, buf.readUnsignedShort());
                break;
            case 66:
                position.set(Position.KEY_POWER, buf.readUnsignedShort() + "mV");
                break;
            case 67:
                position.set(Position.KEY_BATTERY, buf.readUnsignedShort() + "mV");
                break;
            case 70:
                position.set("pcbTemp", (length == 4 ? buf.readInt() : buf.readShort()) * 0.1);
                break;
            case 72:
                position.set(Position.PREFIX_TEMP + 1, buf.readInt() * 0.1);
                break;
            case 73:
                position.set(Position.PREFIX_TEMP + 2, buf.readInt() * 0.1);
                break;
            case 74:
                position.set(Position.PREFIX_TEMP + 3, buf.readInt() * 0.1);
                break;
            case 78:
                position.set(Position.KEY_RFID, buf.readLong());
                break;
            case 182:
                position.set(Position.KEY_HDOP, buf.readUnsignedShort() * 0.1);
                break;
            default:
                switch (length) {
                    case 1:
                        position.set(Position.PREFIX_IO + id, buf.readUnsignedByte());
                        break;
                    case 2:
                        position.set(Position.PREFIX_IO + id, buf.readUnsignedShort());
                        break;
                    case 4:
                        position.set(Position.PREFIX_IO + id, buf.readUnsignedInt());
                        break;
                    case 8:
                    default:
                        position.set(Position.PREFIX_IO + id, buf.readLong());
                        break;
                }
                break;
        }
    }
