  public boolean equals(Mutation m) {
    byte[] myData = serializedSnapshot();
    byte[] otherData = m.serializedSnapshot();
    if (Arrays.equals(row, m.row) && entries == m.entries && Arrays.equals(myData, otherData)) {
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
  private byte[] serializedSnapshot() {
    if (buffer != null) {
      return buffer.toArray();
    } else {
      return this.data;
    }
  }
  private TMutation toThrift(boolean serialize) {
    byte[] data;
    if (serialize) {
      this.serialize();
      data = this.data;
    } else {
      data = serializedSnapshot();
    }
    return new TMutation(java.nio.ByteBuffer.wrap(row), java.nio.ByteBuffer.wrap(data), ByteBufferUtil.toByteBuffers(values), entries);
  }
  public TMutation toThrift() {
    return toThrift(true);
  }
  public int hashCode() {
    return toThrift(false).hashCode();
  }
