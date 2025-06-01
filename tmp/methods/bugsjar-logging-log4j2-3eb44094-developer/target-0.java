    public static Level toLevel(String sArg, Level defaultLevel) {
        if (sArg == null) {
            return defaultLevel;
        }
        for (Level level : values()) {
            if (level.name().equals(sArg)) {
                return level;
            }
        }
        return defaultLevel;
    }
