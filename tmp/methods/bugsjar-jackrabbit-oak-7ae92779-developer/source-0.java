    public int compareTo(MapEntry that) {
        return ComparisonChain.start()
                .compare(getHash(), that.getHash())
                .compare(name, that.name)
                .compare(value, that.value)
                .result();
    }
