    private double doRemove(int index) {
        keys[index]   = 0;
        states[index] = REMOVED;
        final double previous = values[index];
        index = changeIndexSign(index);
        --size;
        return previous;
    }
