    public int compareTo(MapEntry that) {
        return ComparisonChain.start()
                .compare(getHash() & HASH_MASK, that.getHash() & HASH_MASK)
                .compare(name, that.name)
                .compare(value, that.value)
                .result();
    }
