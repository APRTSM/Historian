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
  public static Text toText(ByteBuffer byteBuffer) {
    if (byteBuffer == null)
      return null;

    if (byteBuffer.hasArray()) {
      Text result = new Text();
      result.set(byteBuffer.array(), byteBuffer.arrayOffset() + byteBuffer.position(), byteBuffer.remaining());
      return result;
    } else {
      return new Text(toBytes(byteBuffer));
    }
  }
  public static ByteBuffer toByteBuffers(ByteSequence bs) {
    if (bs == null)
      return null;

    if (bs.isBackedByArray()) {
      return ByteBuffer.wrap(bs.getBackingArray(), bs.offset(), bs.length());
    } else {
      return ByteBuffer.wrap(bs.toArray());
    }
  }
  public static byte[] toBytes(ByteBuffer buffer) {
    if (buffer == null)
      return null;
    if (buffer.hasArray()) {
      // did not use buffer.get() because it changes the position
      return Arrays.copyOfRange(buffer.array(), buffer.position() + buffer.arrayOffset(), buffer.limit() + buffer.arrayOffset());
    } else {
      byte[] data = new byte[buffer.remaining()];
      // duplicate inorder to avoid changing position
      buffer.duplicate().get(data);
      return data;
    }
  }
  public static List<byte[]> toBytesList(Collection<ByteBuffer> bytesList) {
    if (bytesList == null)
      return null;
    ArrayList<byte[]> result = new ArrayList<byte[]>(bytesList.size());
    for (ByteBuffer bytes : bytesList) {
      result.add(toBytes(bytes));
    }
    return result;
  }
  public static String toString(ByteBuffer bytes) {
    if (bytes.hasArray()) {
      return new String(bytes.array(), bytes.arrayOffset() + bytes.position(), bytes.remaining(), UTF_8);
    } else {
      return new String(toBytes(bytes), UTF_8);
    }
  }
