    public Reader(ByteBuffer buffer) {
      if (buffer.hasArray() && buffer.array().length == buffer.arrayOffset() + buffer.limit()) {
        offset = buffer.arrayOffset() + buffer.position();
        data = buffer.array();
      } else {
        data = new byte[buffer.remaining()];
        buffer.get(data);
      }
    }
