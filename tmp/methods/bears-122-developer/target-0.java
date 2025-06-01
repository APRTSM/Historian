    private Object decodeInf(Channel channel, SocketAddress remoteAddress, String sentence) {
        Parser parser = new Parser(PATTERN_INF, sentence);
        Position position = initPosition(parser, channel, remoteAddress);
        if (position == null) {
            return null;
        }

        position.set(Position.KEY_STATUS, parser.next());

        position.set(Position.KEY_RSSI, parser.nextInt());

        parser.next(); // odometer or external power

        position.set(Position.KEY_BATTERY, parser.nextDouble(0));
        position.set(Position.KEY_CHARGE, parser.nextInt(0) == 1);

        position.set(Position.KEY_BATTERY_LEVEL, parser.nextInt());

        position.set(Position.PREFIX_TEMP + 1, parser.next());

        position.set(Position.PREFIX_ADC + 1, parser.next());
        position.set(Position.PREFIX_ADC + 2, parser.next());

        position.set(Position.KEY_INPUT, parser.next());
        position.set(Position.KEY_OUTPUT, parser.next());

        getLastLocation(position, parser.nextDateTime());

        position.set(Position.KEY_INDEX, parser.nextHexInt(0));

        return position;
    }
