    boolean includes(@Nonnull Revision r) {
        return high.getClusterId() == r.getClusterId()
                && high.compareRevisionTime(r) >= 0
                && low.compareRevisionTime(r) <= 0;
    }
