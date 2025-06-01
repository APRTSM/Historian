  @Override public JsonWriter value(Boolean value) throws IOException {
    if (value == null) {
      return nullValue();
    }
    put(new JsonPrimitive(value));
    return this;
  }
