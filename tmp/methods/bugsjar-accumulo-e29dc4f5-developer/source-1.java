  public void closeWriter(String writer) throws TException {
    try {
      BatchWriter batchwriter = writerCache.getIfPresent(UUID.fromString(writer));
      if (batchwriter == null) {
        throw new UnknownWriter("Writer never existed or no longer exists");
      }
      batchwriter.close();
      writerCache.invalidate(UUID.fromString(writer));
    } catch (Exception e) {
      throw translateException(e);
    }
  }
  public void updateAndFlush(ByteBuffer login, String tableName, Map<ByteBuffer,List<ColumnUpdate>> cells) throws TException {
    try {
      BatchWriter writer = getWriter(login, tableName, null);
      addCellsToWriter(cells, writer);
      writer.flush();
      writer.close();
    } catch (Exception e) {
      throw translateException(e);
    }
  }
  private BatchWriter getWriter(ByteBuffer login, String tableName, WriterOptions opts) throws Exception {
    BatchWriterConfig cfg = new BatchWriterConfig();
    if (opts != null) {
      if (opts.maxMemory != 0)
        cfg.setMaxMemory(opts.maxMemory);
      if (opts.threads != 0)
        cfg.setMaxWriteThreads(opts.threads);
      if (opts.timeoutMs != 0)
        cfg.setTimeout(opts.timeoutMs, TimeUnit.MILLISECONDS);
      if (opts.latencyMs != 0)
        cfg.setMaxLatency(opts.latencyMs, TimeUnit.MILLISECONDS);
    }
    return getConnector(login).createBatchWriter(tableName, cfg);
  }
  private void addCellsToWriter(Map<ByteBuffer,List<ColumnUpdate>> cells, BatchWriter writer) throws MutationsRejectedException {
    HashMap<Text,ColumnVisibility> vizMap = new HashMap<Text,ColumnVisibility>();
    
    for (Entry<ByteBuffer,List<ColumnUpdate>> entry : cells.entrySet()) {
      Mutation m = new Mutation(ByteBufferUtil.toBytes(entry.getKey()));
      
      for (ColumnUpdate update : entry.getValue()) {
        ColumnVisibility viz = EMPTY_VIS;
        if (update.isSetColVisibility()) {
          Text vizText = new Text(update.getColVisibility());
          viz = vizMap.get(vizText);
          if (viz == null) {
            vizMap.put(vizText, viz = new ColumnVisibility(vizText));
          }
        }
        byte[] value = new byte[0];
        if (update.isSetValue())
          value = update.getValue();
        if (update.isSetTimestamp()) {
          if (update.isSetDeleteCell()) {
            m.putDelete(update.getColFamily(), update.getColQualifier(), viz, update.getTimestamp());
          } else {
            if (update.isSetDeleteCell()) {
              m.putDelete(update.getColFamily(), update.getColQualifier(), viz, update.getTimestamp());
            } else {
              m.put(update.getColFamily(), update.getColQualifier(), viz, update.getTimestamp(), value);
            }
          }
        } else {
          m.put(update.getColFamily(), update.getColQualifier(), viz, value);
        }
      }
      writer.addMutation(m);
    }
  }
  public String createWriter(ByteBuffer login, String tableName, WriterOptions opts) throws TException {
    try {
      BatchWriter writer = getWriter(login, tableName, opts);
      UUID uuid = UUID.randomUUID();
      writerCache.put(uuid, writer);
      return uuid.toString();
    } catch (Exception e) {
      throw translateException(e);
    }
  }
    public void onRemoval(RemovalNotification<UUID,BatchWriter> notification) {
      try {
        notification.getValue().close();
      } catch (MutationsRejectedException e) {
        logger.warn(e, e);
      }
    }
  public void update(String writer, Map<ByteBuffer,List<ColumnUpdate>> cells) throws TException {
    try {
      BatchWriter batchwriter = writerCache.getIfPresent(UUID.fromString(writer));
      if (batchwriter == null) {
        throw new UnknownWriter("Writer never existed or no longer exists");
      }
      addCellsToWriter(cells, batchwriter);
    } catch (Exception e) {
      throw translateException(e);
    }
  }
  public void flush(String writer) throws TException {
    try {
      BatchWriter batchwriter = writerCache.getIfPresent(UUID.fromString(writer));
      if (batchwriter == null) {
        throw new UnknownWriter("Writer never existed or no longer exists");
      }
      batchwriter.flush();
    } catch (Exception e) {
      throw translateException(e);
    }
  }
    public MutationsRejectedException getOuch2() {
      return this.ouch2;
    }
    public void unsetOuch2() {
      this.ouch2 = null;
    }
      protected boolean isOneway() {
        return false;
      }
    public boolean isSetOuch2() {
      return this.ouch2 != null;
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case OUCH1:
        return getOuch1();

      case OUCH2:
        return getOuch2();

      }
      throw new IllegalStateException();
    }
      public update_resultTupleScheme getScheme() {
        return new update_resultTupleScheme();
      }
      public update_result getResult(I iface, update_args args) throws org.apache.thrift.TException {
        update_result result = new update_result();
        try {
          iface.update(args.writer, args.cells);
        } catch (UnknownWriter ouch1) {
          result.ouch1 = ouch1;
        } catch (MutationsRejectedException ouch2) {
          result.ouch2 = ouch2;
        }
        return result;
      }
    public int hashCode() {
      return 0;
    }
    public update_result setOuch1(UnknownWriter ouch1) {
      this.ouch1 = ouch1;
      return this;
    }
    public boolean isSet(_Fields field) {
      if (field == null) {
        throw new IllegalArgumentException();
      }

      switch (field) {
      case OUCH1:
        return isSetOuch1();
      case OUCH2:
        return isSetOuch2();
      }
      throw new IllegalStateException();
    }
    public int compareTo(update_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;
      update_result typedOther = (update_result)other;

      lastComparison = Boolean.valueOf(isSetOuch1()).compareTo(typedOther.isSetOuch1());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetOuch1()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ouch1, typedOther.ouch1);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      lastComparison = Boolean.valueOf(isSetOuch2()).compareTo(typedOther.isSetOuch2());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetOuch2()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ouch2, typedOther.ouch2);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
      public void write(org.apache.thrift.protocol.TProtocol prot, update_result struct) throws org.apache.thrift.TException {
        TTupleProtocol oprot = (TTupleProtocol) prot;
        BitSet optionals = new BitSet();
        if (struct.isSetOuch1()) {
          optionals.set(0);
        }
        if (struct.isSetOuch2()) {
          optionals.set(1);
        }
        oprot.writeBitSet(optionals, 2);
        if (struct.isSetOuch1()) {
          struct.ouch1.write(oprot);
        }
        if (struct.isSetOuch2()) {
          struct.ouch2.write(oprot);
        }
      }
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
      try {
        read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
      } catch (org.apache.thrift.TException te) {
        throw new java.io.IOException(te);
      }
    }
    public void recv_update() throws UnknownWriter, MutationsRejectedException, org.apache.thrift.TException
    {
      update_result result = new update_result();
      receiveBase(result, "update");
      if (result.ouch1 != null) {
        throw result.ouch1;
      }
      if (result.ouch2 != null) {
        throw result.ouch2;
      }
      return;
    }
    public void setOuch2IsSet(boolean value) {
      if (!value) {
        this.ouch2 = null;
      }
    }
    public void validate() throws org.apache.thrift.TException {
      // check for required fields
      // check for sub-struct validity
    }
    public update_result(
      UnknownWriter ouch1,
      MutationsRejectedException ouch2)
    {
      this();
      this.ouch1 = ouch1;
      this.ouch2 = ouch2;
    }
    public void clear() {
      this.ouch1 = null;
      this.ouch2 = null;
    }
      public update_resultStandardScheme getScheme() {
        return new update_resultStandardScheme();
      }
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
      try {
        write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
      } catch (org.apache.thrift.TException te) {
        throw new java.io.IOException(te);
      }
    }
      public void read(org.apache.thrift.protocol.TProtocol prot, update_result struct) throws org.apache.thrift.TException {
        TTupleProtocol iprot = (TTupleProtocol) prot;
        BitSet incoming = iprot.readBitSet(2);
        if (incoming.get(0)) {
          struct.ouch1 = new UnknownWriter();
          struct.ouch1.read(iprot);
          struct.setOuch1IsSet(true);
        }
        if (incoming.get(1)) {
          struct.ouch2 = new MutationsRejectedException();
          struct.ouch2.read(iprot);
          struct.setOuch2IsSet(true);
        }
      }
    public String toString() {
      StringBuilder sb = new StringBuilder("update_result(");
      boolean first = true;

      sb.append("ouch1:");
      if (this.ouch1 == null) {
        sb.append("null");
      } else {
        sb.append(this.ouch1);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("ouch2:");
      if (this.ouch2 == null) {
        sb.append("null");
      } else {
        sb.append(this.ouch2);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
      public void read(org.apache.thrift.protocol.TProtocol iprot, update_result struct) throws org.apache.thrift.TException {
        org.apache.thrift.protocol.TField schemeField;
        iprot.readStructBegin();
        while (true)
        {
          schemeField = iprot.readFieldBegin();
          if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
            break;
          }
          switch (schemeField.id) {
            case 1: // OUCH1
              if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                struct.ouch1 = new UnknownWriter();
                struct.ouch1.read(iprot);
                struct.setOuch1IsSet(true);
              } else { 
                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
              }
              break;
            case 2: // OUCH2
              if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                struct.ouch2 = new MutationsRejectedException();
                struct.ouch2.read(iprot);
                struct.setOuch2IsSet(true);
              } else { 
                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
              }
              break;
            default:
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
          }
          iprot.readFieldEnd();
        }
        iprot.readStructEnd();

        // check for required fields of primitive type, which can't be checked in the validate method
        struct.validate();
      }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case OUCH1:
        if (value == null) {
          unsetOuch1();
        } else {
          setOuch1((UnknownWriter)value);
        }
        break;

      case OUCH2:
        if (value == null) {
          unsetOuch2();
        } else {
          setOuch2((MutationsRejectedException)value);
        }
        break;

      }
    }
    public update_result setOuch2(MutationsRejectedException ouch2) {
      this.ouch2 = ouch2;
      return this;
    }
    public update_result deepCopy() {
      return new update_result(this);
    }
    public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
      schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
      }
    public update_result() {
    }
    public void update(String writer, Map<ByteBuffer,List<ColumnUpdate>> cells) throws UnknownWriter, MutationsRejectedException, org.apache.thrift.TException
    {
      send_update(writer, cells);
      recv_update();
    }
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof update_result)
        return this.equals((update_result)that);
      return false;
    }
    public boolean equals(update_result that) {
      if (that == null)
        return false;

      boolean this_present_ouch1 = true && this.isSetOuch1();
      boolean that_present_ouch1 = true && that.isSetOuch1();
      if (this_present_ouch1 || that_present_ouch1) {
        if (!(this_present_ouch1 && that_present_ouch1))
          return false;
        if (!this.ouch1.equals(that.ouch1))
          return false;
      }

      boolean this_present_ouch2 = true && this.isSetOuch2();
      boolean that_present_ouch2 = true && that.isSetOuch2();
      if (this_present_ouch2 || that_present_ouch2) {
        if (!(this_present_ouch2 && that_present_ouch2))
          return false;
        if (!this.ouch2.equals(that.ouch2))
          return false;
      }

      return true;
    }
    public boolean isSetOuch1() {
      return this.ouch1 != null;
    }
    public UnknownWriter getOuch1() {
      return this.ouch1;
    }
    public void unsetOuch1() {
      this.ouch1 = null;
    }
    public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
      schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
    }
    public void setOuch1IsSet(boolean value) {
      if (!value) {
        this.ouch1 = null;
      }
    }
    public _Fields fieldForId(int fieldId) {
      return _Fields.findByThriftId(fieldId);
    }
      public void getResult() throws UnknownWriter, MutationsRejectedException, org.apache.thrift.TException {
        if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
        org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        (new Client(prot)).recv_update();
      }
    public update_result(update_result other) {
      if (other.isSetOuch1()) {
        this.ouch1 = new UnknownWriter(other.ouch1);
      }
      if (other.isSetOuch2()) {
        this.ouch2 = new MutationsRejectedException(other.ouch2);
      }
    }
      public void write(org.apache.thrift.protocol.TProtocol oprot, update_result struct) throws org.apache.thrift.TException {
        struct.validate();

        oprot.writeStructBegin(STRUCT_DESC);
        if (struct.ouch1 != null) {
          oprot.writeFieldBegin(OUCH1_FIELD_DESC);
          struct.ouch1.write(oprot);
          oprot.writeFieldEnd();
        }
        if (struct.ouch2 != null) {
          oprot.writeFieldBegin(OUCH2_FIELD_DESC);
          struct.ouch2.write(oprot);
          oprot.writeFieldEnd();
        }
        oprot.writeFieldStop();
        oprot.writeStructEnd();
      }
