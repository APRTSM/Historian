    public void setMaxMemory(long maxMemory) {
        if (maxMemory < 0) {
            throw new IllegalArgumentException("Max memory must not be negative");
        }
        this.maxMemory = maxMemory;
        if (segments != null) {
            long max = 1 + maxMemory / segments.length;
            for (Segment<K, V> s : segments) {
                s.setMaxMemory(max);
            }
        }
    }
