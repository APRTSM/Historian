    public void addValue(Object v) {
            if (!(v instanceof Comparable<?>))throw new java.lang.IllegalArgumentException();
            addValue((Comparable<?>) v);            
    }
