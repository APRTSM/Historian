  public ArrayByteSequence(ByteBuffer buffer) {
    if (buffer.hasArray()) {
      this.data = buffer.array();
      this.offset = buffer.position() + buffer.arrayOffset();
      this.length = buffer.remaining();
    } else {
      this.offset = 0;
      this.data = ByteBufferUtil.toBytes(buffer);
      this.length = data.length;
    }
  }
