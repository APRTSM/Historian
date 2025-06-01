  protected Key buildDocKey() {
    if (log.isTraceEnabled())
      log.trace("building doc key for " + currentPartition + " " + currentDocID);
    int zeroIndex = currentDocID.find("\0");
    if (zeroIndex < 0)
      throw new IllegalArgumentException("bad current docID");
    Text colf = new Text(docColf);
    colf.append(nullByte, 0, 1);
    colf.append(currentDocID.getBytes(), 0, zeroIndex);
    docColfSet = Collections.singleton((ByteSequence) new ArrayByteSequence(colf.getBytes(), 0, colf.getLength()));
    if (log.isTraceEnabled())
      log.trace(zeroIndex + " " + currentDocID.getLength());
    Text colq = new Text();
    colq.set(currentDocID.getBytes(), zeroIndex + 1, currentDocID.getLength() - zeroIndex - 2);
    Key k = new Key(currentPartition, colf, colq);
    if (log.isTraceEnabled())
      log.trace("built doc key for seek: " + k.toString());
    return k;
  }
