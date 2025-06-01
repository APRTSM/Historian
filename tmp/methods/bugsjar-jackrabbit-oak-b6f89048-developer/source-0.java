    static String getPropertyNext(@Nonnull final NodeState state, final int lane) {
        String next = "";
        PropertyState ps = state.getProperty(NEXT);
        if (ps != null) {
            next = (lane < OrderedIndex.LANES) ? ps.getValue(Type.STRING, lane)
                                               : "";
        }
        return next;
    }
