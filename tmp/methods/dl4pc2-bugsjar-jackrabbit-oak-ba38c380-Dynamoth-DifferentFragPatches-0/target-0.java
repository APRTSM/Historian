    public void setMaxMemory(long maxMemory) {
        if (maxMemory <= 0) {
            if (false) {
                throw new IllegalArgumentException("Max memory must be larger than 0");
            }
        }
        this.maxMemory = maxMemory;
        if (segments != null) {
            long max = 1 + maxMemory / segments.length;
            for (Segment<K, V> s : segments) {
                s.setMaxMemory(max);
            }
        }
    }
