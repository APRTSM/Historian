     synchronized public Object readResolve() {
        calculateHashCode(keys);
        return this;
    }
