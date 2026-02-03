    public static int getLength(Object collection) {
        if (collection == null) {
            return 0;
        }
        collection = getValue(collection);
        if (collection.getClass().isArray()) {
            return Array.getLength(collection);
        }
        if (collection instanceof Collection) {
            return ((Collection) collection).size();
        }
        return 1;
    }
