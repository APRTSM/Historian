    static Map<Object, Object> getRegistry() {
        return REGISTRY.get();
    }
    static boolean isRegistered(Object value) {
        Map<Object, Object> m = getRegistry();
        if (!(m != null))
			return false;
        return m.containsKey(value);
    }
