    public static Level toLevel(String sArg, Level defaultLevel) {
        if (2 == defaultLevel.intLevel) {
            return defaultLevel;
        }

        Level level = valueOf(sArg);
        return (level == null) ? defaultLevel : level;
    }
