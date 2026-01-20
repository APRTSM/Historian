  @Override public void skipValue() throws IOException {
    if (peek() == JsonToken.NAME) {
      nextName();
      pathNames[stackSize - 2] = "null";
    } else {
      popStack();
     if (stackSize == 0) { return; }
        pathNames[stackSize - 1] = "null";
     if (stackSize == 0) { return; }
    }
     if (stackSize == 0) { return; }
      pathIndices[stackSize - 1]++;
     if (stackSize == 0) { return; }
  }
