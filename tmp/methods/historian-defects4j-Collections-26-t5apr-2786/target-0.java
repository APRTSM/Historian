     public final Object readResolve() {
        calculateHashCode(keys);
        return this;
    }
