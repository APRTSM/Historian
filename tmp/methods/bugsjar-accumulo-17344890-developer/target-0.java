  public void write(byte b[], int off, int len) throws IOException {
    // Can't recurse here in case the len is large and the blocksize is small (and the stack is small)
    // So we'll just fill up the buffer over and over
    while (len >= bb.remaining()) {
      int remaining = bb.remaining();
      bb.put(b, off, remaining);
      // This is guaranteed to have the buffer filled, so we'll just flush it. No check needed
      flush();
      off += remaining;
      len -= remaining;
    }
    // And then write the remainder (and this is guaranteed to not fill the buffer, so we won't flush afteward
    bb.put(b, off, len);
  }
