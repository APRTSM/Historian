    public String toString() {
        Map<String, String> map = localMap.get();
        return map == null ? "{}" : map.toString();
    }
