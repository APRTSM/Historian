    public boolean isEmpty() {
        final Map<String, String> map = localMap.get();
        return map == null || map.size() == 0;
    }
