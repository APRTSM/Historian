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
  public static String toString(ByteBuffer bytes) {
    return new String(bytes.array(), bytes.position(), bytes.remaining(), UTF_8);
  }
  public static Text toText(ByteBuffer bytes) {
    if (bytes == null)
      return null;
    Text result = new Text();
    result.set(bytes.array(), bytes.position(), bytes.remaining());
    return result;
  }
  public static ByteBuffer toByteBuffers(ByteSequence bs) {
    if (bs == null)
      return null;

    if (bs.isBackedByArray()) {
      return ByteBuffer.wrap(bs.getBackingArray(), bs.offset(), bs.length());
    } else {
      // TODO create more efficient impl
      return ByteBuffer.wrap(bs.toArray());
    }
  }
  public static List<byte[]> toBytesList(Collection<ByteBuffer> bytesList) {
    if (bytesList == null)
      return null;
    ArrayList<byte[]> result = new ArrayList<byte[]>();
    for (ByteBuffer bytes : bytesList) {
      result.add(toBytes(bytes));
    }
    return result;
  }
  public static byte[] toBytes(ByteBuffer buffer) {
    if (buffer == null)
      return null;
    return Arrays.copyOfRange(buffer.array(), buffer.position(), buffer.limit());
  }
