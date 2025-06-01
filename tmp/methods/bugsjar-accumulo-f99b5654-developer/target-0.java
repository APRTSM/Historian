  public void readFields(DataInput in) throws IOException {
    byte[] len = new byte[7];
    in.readFully(len);
    String strLen = new String(len, Charset.forName("UTF-8"));
    if (!strLen.endsWith("#"))
      throw new IllegalStateException("length was not encoded correctly");
    byte[] bytes = new byte[Integer.parseInt(strLen.substring(strLen.lastIndexOf(' ') + 1, strLen.length() - 1), 36)];
    in.readFully(bytes);

    String strFields = new String(bytes, Charset.forName("UTF-8"));
    String[] fields = StringUtils.split(strFields, '\\', ',');
    for (String field : fields) {
      String[] keyValue = StringUtils.split(field, '\\', '=');
      String key = keyValue[0];
      String value = keyValue[1];
      if ("maxMemory".equals(key)) {
        maxMemory = Long.valueOf(value);
      } else if ("maxLatency".equals(key)) {
        maxLatency = Long.valueOf(value);
      } else if ("maxWriteThreads".equals(key)) {
        maxWriteThreads = Integer.valueOf(value);
      } else if ("timeout".equals(key)) {
        timeout = Long.valueOf(value);
      } else {
        /* ignore any other properties */
      }
    }
  }
  public int hashCode() {
    HashCodeBuilder hcb = new HashCodeBuilder();
    hcb.append(maxMemory).append(maxLatency).append(maxWriteThreads).append(timeout);
    return hcb.toHashCode();
  }
  public BatchWriterConfig setTimeout(long timeout, TimeUnit timeUnit) {
    if (timeout < 0)
      throw new IllegalArgumentException("Negative timeout not allowed " + timeout);

    if (timeout == 0)
      this.timeout = Long.MAX_VALUE;
    else
      // make small, positive values that truncate to 0 when converted use the minimum millis instead
      this.timeout = Math.max(1, timeUnit.toMillis(timeout));
    return this;
  }
  public BatchWriterConfig setMaxWriteThreads(int maxWriteThreads) {
    if (maxWriteThreads <= 0)
      throw new IllegalArgumentException("Max threads must be positive " + maxWriteThreads);

    this.maxWriteThreads = maxWriteThreads;
    return this;
  }
  public BatchWriterConfig setMaxLatency(long maxLatency, TimeUnit timeUnit) {
    if (maxLatency < 0)
      throw new IllegalArgumentException("Negative max latency not allowed " + maxLatency);

    if (maxLatency == 0)
      this.maxLatency = Long.MAX_VALUE;
    else
      // make small, positive values that truncate to 0 when converted use the minimum millis instead
      this.maxLatency = Math.max(1, timeUnit.toMillis(maxLatency));
    return this;
  }
  public void write(DataOutput out) throws IOException {
    // write this out in a human-readable way
    ArrayList<String> fields = new ArrayList<String>();
    if (maxMemory != null)
      addField(fields, "maxMemory", maxMemory);
    if (maxLatency != null)
      addField(fields, "maxLatency", maxLatency);
    if (maxWriteThreads != null)
      addField(fields, "maxWriteThreads", maxWriteThreads);
    if (timeout != null)
      addField(fields, "timeout", timeout);
    String output = StringUtils.join(",", fields);

    byte[] bytes = output.getBytes(Charset.forName("UTF-8"));
    byte[] len = String.format("%6s#", Integer.toString(bytes.length, 36)).getBytes("UTF-8");
    if (len.length != 7)
      throw new IllegalStateException("encoded length does not match expected value");
    out.write(len);
    out.write(bytes);
  }
  public boolean equals(Object o) {
    if (o instanceof BatchWriterConfig) {
      BatchWriterConfig other = (BatchWriterConfig) o;

      if (null != maxMemory) {
        if (!maxMemory.equals(other.maxMemory)) {
          return false;
        }
      } else {
        if (null != other.maxMemory) {
          return false;
        }
      }

      if (null != maxLatency) {
        if (!maxLatency.equals(other.maxLatency)) {
          return false;
        }
      } else {
        if (null != other.maxLatency) {
          return false;
        }
      }

      if (null != maxWriteThreads) {
        if (!maxWriteThreads.equals(other.maxWriteThreads)) {
          return false;
        }
      } else {
        if (null != other.maxWriteThreads) {
          return false;
        }
      }

      if (null != timeout) {
        if (!timeout.equals(other.timeout)) {
          return false;
        }
      } else {
        if (null != other.timeout) {
          return false;
        }
      }

      return true;
    }

    return false;
  }
  public String toString() {
    StringBuilder sb = new StringBuilder(32);
    sb.append("[maxMemory=").append(getMaxMemory()).append(", maxLatency=").append(getMaxLatency(TimeUnit.MILLISECONDS)).append(", maxWriteThreads=")
        .append(getMaxWriteThreads()).append(", timeout=").append(getTimeout(TimeUnit.MILLISECONDS)).append("]");
    return sb.toString();
  }
