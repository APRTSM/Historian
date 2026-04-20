    public InnerClassProperty withValueDeserializer(JsonDeserializer<?> deser) {
        return new InnerClassProperty(this, deser);
    }
