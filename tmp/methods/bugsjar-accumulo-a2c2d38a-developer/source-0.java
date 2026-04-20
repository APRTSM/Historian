    public Reader(ByteBuffer buffer) {
      if (buffer.hasArray()) {
        offset = buffer.arrayOffset();
        data = buffer.array();
      } else {
        data = new byte[buffer.remaining()];
        buffer.get(data);
      }
    }
