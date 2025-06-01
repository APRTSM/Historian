    public String toString() {
        Map<String, String> map = localMap.get();
        return map == null ? "{}" : map.toString();
    }
    public String toString() {
        final List<String> list = stack.get();
        return list == null ? "[]" : list.toString();
    }
