  public ArrayByteSequence(ByteBuffer buffer) {
    this.length = buffer.remaining();

    if (buffer.hasArray()) {
      this.data = buffer.array();
      this.offset = buffer.position();
    } else {
      this.data = new byte[length];
      this.offset = 0;
      buffer.get(data);
    }
  }
