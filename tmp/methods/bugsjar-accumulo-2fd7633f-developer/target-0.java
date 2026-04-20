  public void readFields(DataInput in) throws IOException {
    range.readFields(in);
    int numLocs = in.readInt();
    locations = new String[numLocs];
    for (int i = 0; i < numLocs; ++i)
      locations[i] = in.readUTF();

    if (in.readBoolean()) {
      isolatedScan = in.readBoolean();
    }

    if (in.readBoolean()) {
      offline = in.readBoolean();
    }

    if (in.readBoolean()) {
      localIterators = in.readBoolean();
    }

    if (in.readBoolean()) {
      mockInstance = in.readBoolean();
    }

    if (in.readBoolean()) {
      int numColumns = in.readInt();
      List<String> columns = new ArrayList<String>(numColumns);
      for (int i = 0; i < numColumns; i++) {
        columns.add(in.readUTF());
      }

      fetchedColumns = InputConfigurator.deserializeFetchedColumns(columns);
    }

    if (in.readBoolean()) {
      String strAuths = in.readUTF();
      auths = new Authorizations(strAuths.getBytes(Charset.forName("UTF-8")));
    }

    if (in.readBoolean()) {
      principal = in.readUTF();
    }

    if (in.readBoolean()) {
      String tokenClass = in.readUTF();
      byte[] base64TokenBytes = in.readUTF().getBytes(Charset.forName("UTF-8"));
      byte[] tokenBytes = Base64.decodeBase64(base64TokenBytes);

      try {
        token = CredentialHelper.extractToken(tokenClass, tokenBytes);
      } catch (AccumuloSecurityException e) {
        throw new IOException(e);
      }
    }

    if (in.readBoolean()) {
      instanceName = in.readUTF();
    }

    if (in.readBoolean()) {
      zooKeepers = in.readUTF();
    }

    if (in.readBoolean()) {
      int numIterators = in.readInt();
      iterators = new ArrayList<IteratorSetting>(numIterators);
      for (int i = 0; i < numIterators; i++) {
        iterators.add(new IteratorSetting(in));
      }
    }

    if (in.readBoolean()) {
      level = Level.toLevel(in.readInt());
    }
  }
  public void write(DataOutput out) throws IOException {
    range.write(out);
    out.writeInt(locations.length);
    for (int i = 0; i < locations.length; ++i)
      out.writeUTF(locations[i]);

    out.writeBoolean(null != isolatedScan);
    if (null != isolatedScan) {
      out.writeBoolean(isolatedScan);
    }

    out.writeBoolean(null != offline);
    if (null != offline) {
      out.writeBoolean(offline);
    }

    out.writeBoolean(null != localIterators);
    if (null != localIterators) {
      out.writeBoolean(localIterators);
    }

    out.writeBoolean(null != mockInstance);
    if (null != mockInstance) {
      out.writeBoolean(mockInstance);
    }

    out.writeBoolean(null != fetchedColumns);
    if (null != fetchedColumns) {
      String[] cols = InputConfigurator.serializeColumns(fetchedColumns);
      out.writeInt(cols.length);
      for (String col : cols) {
        out.writeUTF(col);
      }
    }

    out.writeBoolean(null != auths);
    if (null != auths) {
      out.writeUTF(auths.serialize());
    }

    out.writeBoolean(null != principal);
    if (null != principal) {
      out.writeUTF(principal);
    }

    out.writeBoolean(null != token);
    if (null != token) {
      out.writeUTF(token.getClass().getCanonicalName());
      try {
        out.writeUTF(CredentialHelper.tokenAsBase64(token));
      } catch (AccumuloSecurityException e) {
        throw new IOException(e);
      }
    }

    out.writeBoolean(null != instanceName);
    if (null != instanceName) {
      out.writeUTF(instanceName);
    }

    out.writeBoolean(null != zooKeepers);
    if (null != zooKeepers) {
      out.writeUTF(zooKeepers);
    }

    out.writeBoolean(null != iterators);
    if (null != iterators) {
      out.writeInt(iterators.size());
      for (IteratorSetting iterator : iterators) {
        iterator.write(out);
      }
    }

    out.writeBoolean(null != level);
    if (null != level) {
      out.writeInt(level.toInt());
    }
  }
