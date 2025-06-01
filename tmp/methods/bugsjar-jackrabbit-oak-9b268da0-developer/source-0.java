    public MongoNode copy() {
        MongoNode copy = new MongoNode();
        copy.putAll((Map) super.copy());
        return copy;
    }
