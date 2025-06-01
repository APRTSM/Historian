    boolean includes(Revision r) {
        return high.compareRevisionTime(r) >= 0
                && low.compareRevisionTime(r) <= 0;
    }
