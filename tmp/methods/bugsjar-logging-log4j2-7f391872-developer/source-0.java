    public static int getPriority(Facility facility, Level level) {
        return facility.getCode() << 3 + Severity.getSeverity(level).getCode();
    }
