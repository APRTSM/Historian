  public int hashCode() {
    return toThrift().hashCode();
  }
  public boolean equals(Mutation m) {
    serialize();
    m.serialize();
    if (Arrays.equals(row, m.row) && entries == m.entries && Arrays.equals(data, m.data)) {
      if (values == null && m.values == null)
        return true;

      if (values != null && m.values != null && values.size() == m.values.size()) {
        for (int i = 0; i < values.size(); i++) {
          if (!Arrays.equals(values.get(i), m.values.get(i)))
            return false;
        }

        return true;
      }

    }

    return false;
  }
  public TMutation toThrift() {
    serialize();
    return new TMutation(java.nio.ByteBuffer.wrap(row), java.nio.ByteBuffer.wrap(data), ByteBufferUtil.toByteBuffers(values), entries);
  }
