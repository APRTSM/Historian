    static String getPropertyNext(@Nonnull final NodeState state, final int lane) {
        String next = "";
        PropertyState ps = state.getProperty(NEXT);
        if (ps != null) {
            if (ps.isArray()) {
                next = ps.getValue(Type.STRING, Math.min(ps.count() - 1, lane));
            } else {
                next = ps.getValue(Type.STRING);
            }
        }
        return next;
    }
