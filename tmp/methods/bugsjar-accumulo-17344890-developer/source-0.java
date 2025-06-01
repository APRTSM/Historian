  public void write(byte b[], int off, int len) throws IOException {
    if (bb.remaining() >= len) {
      bb.put(b, off, len);
      if (bb.remaining() == 0)
        flush();
    } else {
      int remaining = bb.remaining();
      write(b, off, remaining);
      write(b, off + remaining, len - remaining);
    }
  }
