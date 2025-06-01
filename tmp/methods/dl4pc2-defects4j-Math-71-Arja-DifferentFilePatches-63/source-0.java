    public boolean stop() {
        for (EventState state : states) {
            if (state.stop()) {
                return true;
            }
        }
        return false;
    }
