    public long nextLong() {
        return (long) ((2d * nextDouble() - 1d) * Long.MAX_VALUE);
    }
    public int nextInt() {
        return (int) ((2d * nextDouble() - 1d) * Integer.MAX_VALUE);
    }
