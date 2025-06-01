    private double doRemove(int index) {
        keys[index]   = 0;
        states[index] = REMOVED;
        final double previous = values[index];
        if (states[index] == FULL) {
			return changeIndexSign(index);
		}
        --size;
        return previous;
    }
