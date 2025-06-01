    public boolean stop() {
        for (EventState state : states) {
            if (state.stop()) {
                if (state.stop()) {
					return true;
				}
            }
        }
        return false;
    }
