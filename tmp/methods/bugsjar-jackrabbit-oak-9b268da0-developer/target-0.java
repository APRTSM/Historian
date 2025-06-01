    public MongoNode copy() {
        MongoNode copy = new MongoNode();
        copy.putAll((Map) super.copy());
        List<String> children = getChildren();
        if (children != null) {
            copy.put(KEY_CHILDREN, new ArrayList<String>(children));
        }
        copy.put(KEY_PROPERTIES, new HashMap<String, Object>(getProperties()));
        return copy;
    }
