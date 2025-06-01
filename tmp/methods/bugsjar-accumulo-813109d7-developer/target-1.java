  public boolean authenticateUser(ByteBuffer login, String principal, Map<String,String> properties) throws TException {
    try {
      return getConnector(login).securityOperations().authenticateUser(principal, getToken(principal, properties));
    } catch (Exception e) {
      throw translateException(e);
    }
  }
  public String createBatchScanner(ByteBuffer login, String tableName, BatchScanOptions opts) throws TException {
    try {
      Connector connector = getConnector(login);
      
      int threads = 10;
      Authorizations auth;
      if (opts != null && opts.isSetAuthorizations()) {
        auth = getAuthorizations(opts.authorizations);
      } else {
        auth = connector.securityOperations().getUserAuthorizations(connector.whoami());
      }
      if (opts != null && opts.threads > 0)
        threads = opts.threads;
      
      BatchScanner scanner = connector.createBatchScanner(tableName, auth, threads);
      
      if (opts != null) {
        if (opts.iterators != null) {
          for (org.apache.accumulo.proxy.thrift.IteratorSetting iter : opts.iterators) {
            IteratorSetting is = new IteratorSetting(iter.getPriority(), iter.getName(), iter.getIteratorClass(), iter.getProperties());
            scanner.addScanIterator(is);
          }
        }
        
        ArrayList<Range> ranges = new ArrayList<Range>();
        
        if (opts.ranges == null) {
          ranges.add(new Range());
        } else {
          for (org.apache.accumulo.proxy.thrift.Range range : opts.ranges) {
            Range aRange = new Range(range.getStart() == null ? null : Util.fromThrift(range.getStart()), true, range.getStop() == null ? null
                : Util.fromThrift(range.getStop()), false);
            ranges.add(aRange);
          }
        }
        scanner.setRanges(ranges);

        if (opts.columns != null) {
          for (ScanColumn col : opts.columns) {
            if (col.isSetColQualifier())
              scanner.fetchColumn(ByteBufferUtil.toText(col.colFamily), ByteBufferUtil.toText(col.colQualifier));
            else
              scanner.fetchColumnFamily(ByteBufferUtil.toText(col.colFamily));
          }
        }
      }

      UUID uuid = UUID.randomUUID();
      
      ScannerPlusIterator spi = new ScannerPlusIterator();
      spi.scanner = scanner;
      spi.iterator = scanner.iterator();
      scannerCache.put(uuid, spi);
      return uuid.toString();
    } catch (Exception e) {
      throw translateException(e);
    }
  }
  public ProxyServer(Properties props) {

    String useMock = props.getProperty("org.apache.accumulo.proxy.ProxyServer.useMockInstance");
    if (useMock != null && Boolean.parseBoolean(useMock))
      instance = new MockInstance();
    else
      instance = new ZooKeeperInstance(props.getProperty("org.apache.accumulo.proxy.ProxyServer.instancename"),
          props.getProperty("org.apache.accumulo.proxy.ProxyServer.zookeepers"));
    
    try {
      tokenClass = Class.forName(props.getProperty("org.apache.accumulo.proxy.ProxyServer.tokenClass")).asSubclass(AuthenticationToken.class);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    scannerCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).maximumSize(1000).removalListener(new CloseScanner()).build();
    
    writerCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).maximumSize(1000).removalListener(new CloseWriter()).build();
  }
  private AuthenticationToken getToken(String principal, Map<String,String> properties) throws AccumuloSecurityException, AccumuloException {
    Properties props = new Properties();
    props.putAll(properties);
    AuthenticationToken token;
    try {
      token = tokenClass.newInstance();
    } catch (InstantiationException e) {
      throw new AccumuloException(e);
    } catch (IllegalAccessException e) {
      throw new AccumuloException(e);
    }
    token.init(props);
    return token;
  }
  private TException translateException(Exception ex) {
    try {
      throw ex;
    } catch (MutationsRejectedException e) {
      logger.debug(e, e);
      return new org.apache.accumulo.proxy.thrift.MutationsRejectedException(e.toString());
    } catch (AccumuloException e) {
      if (e.getCause() instanceof ThriftTableOperationException) {
        ThriftTableOperationException ttoe = (ThriftTableOperationException) e.getCause();
        if (ttoe.type == TableOperationExceptionType.NOTFOUND) {
          return new org.apache.accumulo.proxy.thrift.TableNotFoundException(e.toString());
        }
      }
      logger.debug(e, e);
      return new org.apache.accumulo.proxy.thrift.AccumuloException(e.toString());
    } catch (AccumuloSecurityException e) {
      logger.debug(e, e);
      if (e.getSecurityErrorCode().equals(SecurityErrorCode.TABLE_DOESNT_EXIST))
        return new org.apache.accumulo.proxy.thrift.TableNotFoundException(e.toString());
      return new org.apache.accumulo.proxy.thrift.AccumuloSecurityException(e.toString());
    } catch (TableNotFoundException e) {
      logger.debug(e, e);
      return new org.apache.accumulo.proxy.thrift.TableNotFoundException(e.toString());
    } catch (TableExistsException e) {
      logger.debug(e, e);
      return new org.apache.accumulo.proxy.thrift.TableExistsException(e.toString());
    } catch (RuntimeException e) {
      if (e.getCause() != null) {
        if (e.getCause() instanceof Exception)
          return translateException((Exception) e.getCause());
      }
      return new TException(e);
    } catch (Exception e) {
      return new TException(ex);
    }
  }
    public void clear() {
      this.ouch1 = null;
      this.ouch2 = null;
      this.ouch3 = null;
    }
    public String toString() {
      StringBuilder sb = new StringBuilder("removeConstraint_result(");
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
      if (!first) sb.append(", ");
      sb.append("ouch3:");
      if (this.ouch3 == null) {
        sb.append("null");
      } else {
        sb.append(this.ouch3);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public boolean equals(setTableProperty_result that) {
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

      boolean this_present_ouch3 = true && this.isSetOuch3();
      boolean that_present_ouch3 = true && that.isSetOuch3();
      if (this_present_ouch3 || that_present_ouch3) {
        if (!(this_present_ouch3 && that_present_ouch3))
          return false;
        if (!this.ouch3.equals(that.ouch3))
          return false;
      }

      return true;
    }
    public removeTableProperty_result setOuch3(TableNotFoundException ouch3) {
      this.ouch3 = ouch3;
      return this;
    }
      public void write(org.apache.thrift.protocol.TProtocol oprot, removeConstraint_result struct) throws org.apache.thrift.TException {
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
        if (struct.ouch3 != null) {
          oprot.writeFieldBegin(OUCH3_FIELD_DESC);
          struct.ouch3.write(oprot);
          oprot.writeFieldEnd();
        }
        oprot.writeFieldStop();
        oprot.writeStructEnd();
      }
    public removeConstraint_result(removeConstraint_result other) {
      if (other.isSetOuch1()) {
        this.ouch1 = new AccumuloException(other.ouch1);
      }
      if (other.isSetOuch2()) {
        this.ouch2 = new AccumuloSecurityException(other.ouch2);
      }
      if (other.isSetOuch3()) {
        this.ouch3 = new TableNotFoundException(other.ouch3);
      }
    }
    public boolean equals(removeTableProperty_result that) {
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

      boolean this_present_ouch3 = true && this.isSetOuch3();
      boolean that_present_ouch3 = true && that.isSetOuch3();
      if (this_present_ouch3 || that_present_ouch3) {
        if (!(this_present_ouch3 && that_present_ouch3))
          return false;
        if (!this.ouch3.equals(that.ouch3))
          return false;
      }

      return true;
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case OUCH1:
        if (value == null) {
          unsetOuch1();
        } else {
          setOuch1((AccumuloException)value);
        }
        break;

      case OUCH2:
        if (value == null) {
          unsetOuch2();
        } else {
          setOuch2((AccumuloSecurityException)value);
        }
        break;

      case OUCH3:
        if (value == null) {
          unsetOuch3();
        } else {
          setOuch3((TableNotFoundException)value);
        }
        break;

      }
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
      case OUCH3:
        return isSetOuch3();
      }
      throw new IllegalStateException();
    }
    public setTableProperty_result(setTableProperty_result other) {
      if (other.isSetOuch1()) {
        this.ouch1 = new AccumuloException(other.ouch1);
      }
      if (other.isSetOuch2()) {
        this.ouch2 = new AccumuloSecurityException(other.ouch2);
      }
      if (other.isSetOuch3()) {
        this.ouch3 = new TableNotFoundException(other.ouch3);
      }
    }
    public void unsetOuch3() {
      this.ouch3 = null;
    }
    public void setTableProperty(ByteBuffer login, String tableName, String property, String value) throws AccumuloException, AccumuloSecurityException, TableNotFoundException, org.apache.thrift.TException
    {
      send_setTableProperty(login, tableName, property, value);
      recv_setTableProperty();
    }
    public TableNotFoundException getOuch3() {
      return this.ouch3;
    }
    public setTableProperty_result setOuch3(TableNotFoundException ouch3) {
      this.ouch3 = ouch3;
      return this;
    }
      public void read(org.apache.thrift.protocol.TProtocol prot, setTableProperty_result struct) throws org.apache.thrift.TException {
        TTupleProtocol iprot = (TTupleProtocol) prot;
        BitSet incoming = iprot.readBitSet(3);
        if (incoming.get(0)) {
          struct.ouch1 = new AccumuloException();
          struct.ouch1.read(iprot);
          struct.setOuch1IsSet(true);
        }
        if (incoming.get(1)) {
          struct.ouch2 = new AccumuloSecurityException();
          struct.ouch2.read(iprot);
          struct.setOuch2IsSet(true);
        }
        if (incoming.get(2)) {
          struct.ouch3 = new TableNotFoundException();
          struct.ouch3.read(iprot);
          struct.setOuch3IsSet(true);
        }
      }
      public removeTableProperty_result getResult(I iface, removeTableProperty_args args) throws org.apache.thrift.TException {
        removeTableProperty_result result = new removeTableProperty_result();
        try {
          iface.removeTableProperty(args.login, args.tableName, args.property);
        } catch (AccumuloException ouch1) {
          result.ouch1 = ouch1;
        } catch (AccumuloSecurityException ouch2) {
          result.ouch2 = ouch2;
        } catch (TableNotFoundException ouch3) {
          result.ouch3 = ouch3;
        }
        return result;
      }
    public int compareTo(setTableProperty_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;
      setTableProperty_result typedOther = (setTableProperty_result)other;

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
      lastComparison = Boolean.valueOf(isSetOuch3()).compareTo(typedOther.isSetOuch3());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetOuch3()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ouch3, typedOther.ouch3);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
      public void write(org.apache.thrift.protocol.TProtocol prot, removeConstraint_result struct) throws org.apache.thrift.TException {
        TTupleProtocol oprot = (TTupleProtocol) prot;
        BitSet optionals = new BitSet();
        if (struct.isSetOuch1()) {
          optionals.set(0);
        }
        if (struct.isSetOuch2()) {
          optionals.set(1);
        }
        if (struct.isSetOuch3()) {
          optionals.set(2);
        }
        oprot.writeBitSet(optionals, 3);
        if (struct.isSetOuch1()) {
          struct.ouch1.write(oprot);
        }
        if (struct.isSetOuch2()) {
          struct.ouch2.write(oprot);
        }
        if (struct.isSetOuch3()) {
          struct.ouch3.write(oprot);
        }
      }
    public TableNotFoundException getOuch3() {
      return this.ouch3;
    }
    public void unsetOuch3() {
      this.ouch3 = null;
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case OUCH1:
        return getOuch1();

      case OUCH2:
        return getOuch2();

      case OUCH3:
        return getOuch3();

      }
      throw new IllegalStateException();
    }
    public removeConstraint_result(
      AccumuloException ouch1,
      AccumuloSecurityException ouch2,
      TableNotFoundException ouch3)
    {
      this();
      this.ouch1 = ouch1;
      this.ouch2 = ouch2;
      this.ouch3 = ouch3;
    }
    public boolean isSetOuch3() {
      return this.ouch3 != null;
    }
    public int compareTo(removeTableProperty_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;
      removeTableProperty_result typedOther = (removeTableProperty_result)other;

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
      lastComparison = Boolean.valueOf(isSetOuch3()).compareTo(typedOther.isSetOuch3());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetOuch3()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ouch3, typedOther.ouch3);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
      public void read(org.apache.thrift.protocol.TProtocol prot, removeTableProperty_result struct) throws org.apache.thrift.TException {
        TTupleProtocol iprot = (TTupleProtocol) prot;
        BitSet incoming = iprot.readBitSet(3);
        if (incoming.get(0)) {
          struct.ouch1 = new AccumuloException();
          struct.ouch1.read(iprot);
          struct.setOuch1IsSet(true);
        }
        if (incoming.get(1)) {
          struct.ouch2 = new AccumuloSecurityException();
          struct.ouch2.read(iprot);
          struct.setOuch2IsSet(true);
        }
        if (incoming.get(2)) {
          struct.ouch3 = new TableNotFoundException();
          struct.ouch3.read(iprot);
          struct.setOuch3IsSet(true);
        }
      }
    public String toString() {
      StringBuilder sb = new StringBuilder("setTableProperty_result(");
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
      if (!first) sb.append(", ");
      sb.append("ouch3:");
      if (this.ouch3 == null) {
        sb.append("null");
      } else {
        sb.append(this.ouch3);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public void removeTableProperty(ByteBuffer login, String tableName, String property) throws AccumuloException, AccumuloSecurityException, TableNotFoundException, org.apache.thrift.TException
    {
      send_removeTableProperty(login, tableName, property);
      recv_removeTableProperty();
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case OUCH1:
        return getOuch1();

      case OUCH2:
        return getOuch2();

      case OUCH3:
        return getOuch3();

      }
      throw new IllegalStateException();
    }
    public String toString() {
      StringBuilder sb = new StringBuilder("removeTableProperty_result(");
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
      if (!first) sb.append(", ");
      sb.append("ouch3:");
      if (this.ouch3 == null) {
        sb.append("null");
      } else {
        sb.append(this.ouch3);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }
    public boolean isSetOuch3() {
      return this.ouch3 != null;
    }
      public void write(org.apache.thrift.protocol.TProtocol oprot, setTableProperty_result struct) throws org.apache.thrift.TException {
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
        if (struct.ouch3 != null) {
          oprot.writeFieldBegin(OUCH3_FIELD_DESC);
          struct.ouch3.write(oprot);
          oprot.writeFieldEnd();
        }
        oprot.writeFieldStop();
        oprot.writeStructEnd();
      }
    public void setOuch3IsSet(boolean value) {
      if (!value) {
        this.ouch3 = null;
      }
    }
      public void write(org.apache.thrift.protocol.TProtocol oprot, removeTableProperty_result struct) throws org.apache.thrift.TException {
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
        if (struct.ouch3 != null) {
          oprot.writeFieldBegin(OUCH3_FIELD_DESC);
          struct.ouch3.write(oprot);
          oprot.writeFieldEnd();
        }
        oprot.writeFieldStop();
        oprot.writeStructEnd();
      }
    public void recv_setTableProperty() throws AccumuloException, AccumuloSecurityException, TableNotFoundException, org.apache.thrift.TException
    {
      setTableProperty_result result = new setTableProperty_result();
      receiveBase(result, "setTableProperty");
      if (result.ouch1 != null) {
        throw result.ouch1;
      }
      if (result.ouch2 != null) {
        throw result.ouch2;
      }
      if (result.ouch3 != null) {
        throw result.ouch3;
      }
      return;
    }
    public void recv_removeTableProperty() throws AccumuloException, AccumuloSecurityException, TableNotFoundException, org.apache.thrift.TException
    {
      removeTableProperty_result result = new removeTableProperty_result();
      receiveBase(result, "removeTableProperty");
      if (result.ouch1 != null) {
        throw result.ouch1;
      }
      if (result.ouch2 != null) {
        throw result.ouch2;
      }
      if (result.ouch3 != null) {
        throw result.ouch3;
      }
      return;
    }
    public removeTableProperty_result(removeTableProperty_result other) {
      if (other.isSetOuch1()) {
        this.ouch1 = new AccumuloException(other.ouch1);
      }
      if (other.isSetOuch2()) {
        this.ouch2 = new AccumuloSecurityException(other.ouch2);
      }
      if (other.isSetOuch3()) {
        this.ouch3 = new TableNotFoundException(other.ouch3);
      }
    }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case OUCH1:
        if (value == null) {
          unsetOuch1();
        } else {
          setOuch1((AccumuloException)value);
        }
        break;

      case OUCH2:
        if (value == null) {
          unsetOuch2();
        } else {
          setOuch2((AccumuloSecurityException)value);
        }
        break;

      case OUCH3:
        if (value == null) {
          unsetOuch3();
        } else {
          setOuch3((TableNotFoundException)value);
        }
        break;

      }
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
      case OUCH3:
        return isSetOuch3();
      }
      throw new IllegalStateException();
    }
    public boolean isSetOuch3() {
      return this.ouch3 != null;
    }
      public void getResult() throws AccumuloException, AccumuloSecurityException, TableNotFoundException, org.apache.thrift.TException {
        if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
        org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        (new Client(prot)).recv_setTableProperty();
      }
    public void setFieldValue(_Fields field, Object value) {
      switch (field) {
      case OUCH1:
        if (value == null) {
          unsetOuch1();
        } else {
          setOuch1((AccumuloException)value);
        }
        break;

      case OUCH2:
        if (value == null) {
          unsetOuch2();
        } else {
          setOuch2((AccumuloSecurityException)value);
        }
        break;

      case OUCH3:
        if (value == null) {
          unsetOuch3();
        } else {
          setOuch3((TableNotFoundException)value);
        }
        break;

      }
    }
    public TableNotFoundException getOuch3() {
      return this.ouch3;
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
      case OUCH3:
        return isSetOuch3();
      }
      throw new IllegalStateException();
    }
      public void write(org.apache.thrift.protocol.TProtocol prot, setTableProperty_result struct) throws org.apache.thrift.TException {
        TTupleProtocol oprot = (TTupleProtocol) prot;
        BitSet optionals = new BitSet();
        if (struct.isSetOuch1()) {
          optionals.set(0);
        }
        if (struct.isSetOuch2()) {
          optionals.set(1);
        }
        if (struct.isSetOuch3()) {
          optionals.set(2);
        }
        oprot.writeBitSet(optionals, 3);
        if (struct.isSetOuch1()) {
          struct.ouch1.write(oprot);
        }
        if (struct.isSetOuch2()) {
          struct.ouch2.write(oprot);
        }
        if (struct.isSetOuch3()) {
          struct.ouch3.write(oprot);
        }
      }
      public void read(org.apache.thrift.protocol.TProtocol iprot, removeConstraint_result struct) throws org.apache.thrift.TException {
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
                struct.ouch1 = new AccumuloException();
                struct.ouch1.read(iprot);
                struct.setOuch1IsSet(true);
              } else { 
                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
              }
              break;
            case 2: // OUCH2
              if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                struct.ouch2 = new AccumuloSecurityException();
                struct.ouch2.read(iprot);
                struct.setOuch2IsSet(true);
              } else { 
                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
              }
              break;
            case 3: // OUCH3
              if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                struct.ouch3 = new TableNotFoundException();
                struct.ouch3.read(iprot);
                struct.setOuch3IsSet(true);
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
    public void unsetOuch3() {
      this.ouch3 = null;
    }
    public void removeConstraint(ByteBuffer login, String tableName, int constraint) throws AccumuloException, AccumuloSecurityException, TableNotFoundException, org.apache.thrift.TException
    {
      send_removeConstraint(login, tableName, constraint);
      recv_removeConstraint();
    }
    public setTableProperty_result(
      AccumuloException ouch1,
      AccumuloSecurityException ouch2,
      TableNotFoundException ouch3)
    {
      this();
      this.ouch1 = ouch1;
      this.ouch2 = ouch2;
      this.ouch3 = ouch3;
    }
    public removeConstraint_result setOuch3(TableNotFoundException ouch3) {
      this.ouch3 = ouch3;
      return this;
    }
    public removeTableProperty_result(
      AccumuloException ouch1,
      AccumuloSecurityException ouch2,
      TableNotFoundException ouch3)
    {
      this();
      this.ouch1 = ouch1;
      this.ouch2 = ouch2;
      this.ouch3 = ouch3;
    }
      public void write(org.apache.thrift.protocol.TProtocol prot, removeTableProperty_result struct) throws org.apache.thrift.TException {
        TTupleProtocol oprot = (TTupleProtocol) prot;
        BitSet optionals = new BitSet();
        if (struct.isSetOuch1()) {
          optionals.set(0);
        }
        if (struct.isSetOuch2()) {
          optionals.set(1);
        }
        if (struct.isSetOuch3()) {
          optionals.set(2);
        }
        oprot.writeBitSet(optionals, 3);
        if (struct.isSetOuch1()) {
          struct.ouch1.write(oprot);
        }
        if (struct.isSetOuch2()) {
          struct.ouch2.write(oprot);
        }
        if (struct.isSetOuch3()) {
          struct.ouch3.write(oprot);
        }
      }
    public void recv_removeConstraint() throws AccumuloException, AccumuloSecurityException, TableNotFoundException, org.apache.thrift.TException
    {
      removeConstraint_result result = new removeConstraint_result();
      receiveBase(result, "removeConstraint");
      if (result.ouch1 != null) {
        throw result.ouch1;
      }
      if (result.ouch2 != null) {
        throw result.ouch2;
      }
      if (result.ouch3 != null) {
        throw result.ouch3;
      }
      return;
    }
      public void read(org.apache.thrift.protocol.TProtocol iprot, setTableProperty_result struct) throws org.apache.thrift.TException {
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
                struct.ouch1 = new AccumuloException();
                struct.ouch1.read(iprot);
                struct.setOuch1IsSet(true);
              } else { 
                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
              }
              break;
            case 2: // OUCH2
              if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                struct.ouch2 = new AccumuloSecurityException();
                struct.ouch2.read(iprot);
                struct.setOuch2IsSet(true);
              } else { 
                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
              }
              break;
            case 3: // OUCH3
              if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                struct.ouch3 = new TableNotFoundException();
                struct.ouch3.read(iprot);
                struct.setOuch3IsSet(true);
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
    public boolean equals(removeConstraint_result that) {
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

      boolean this_present_ouch3 = true && this.isSetOuch3();
      boolean that_present_ouch3 = true && that.isSetOuch3();
      if (this_present_ouch3 || that_present_ouch3) {
        if (!(this_present_ouch3 && that_present_ouch3))
          return false;
        if (!this.ouch3.equals(that.ouch3))
          return false;
      }

      return true;
    }
    public void setOuch3IsSet(boolean value) {
      if (!value) {
        this.ouch3 = null;
      }
    }
    public void clear() {
      this.ouch1 = null;
      this.ouch2 = null;
      this.ouch3 = null;
    }
    public void setOuch3IsSet(boolean value) {
      if (!value) {
        this.ouch3 = null;
      }
    }
      public void getResult() throws AccumuloException, AccumuloSecurityException, TableNotFoundException, org.apache.thrift.TException {
        if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
        org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        (new Client(prot)).recv_removeConstraint();
      }
    public void clear() {
      this.ouch1 = null;
      this.ouch2 = null;
      this.ouch3 = null;
    }
      public setTableProperty_result getResult(I iface, setTableProperty_args args) throws org.apache.thrift.TException {
        setTableProperty_result result = new setTableProperty_result();
        try {
          iface.setTableProperty(args.login, args.tableName, args.property, args.value);
        } catch (AccumuloException ouch1) {
          result.ouch1 = ouch1;
        } catch (AccumuloSecurityException ouch2) {
          result.ouch2 = ouch2;
        } catch (TableNotFoundException ouch3) {
          result.ouch3 = ouch3;
        }
        return result;
      }
      public void read(org.apache.thrift.protocol.TProtocol iprot, removeTableProperty_result struct) throws org.apache.thrift.TException {
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
                struct.ouch1 = new AccumuloException();
                struct.ouch1.read(iprot);
                struct.setOuch1IsSet(true);
              } else { 
                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
              }
              break;
            case 2: // OUCH2
              if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                struct.ouch2 = new AccumuloSecurityException();
                struct.ouch2.read(iprot);
                struct.setOuch2IsSet(true);
              } else { 
                org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
              }
              break;
            case 3: // OUCH3
              if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
                struct.ouch3 = new TableNotFoundException();
                struct.ouch3.read(iprot);
                struct.setOuch3IsSet(true);
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
      public void getResult() throws AccumuloException, AccumuloSecurityException, TableNotFoundException, org.apache.thrift.TException {
        if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
        org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        (new Client(prot)).recv_removeTableProperty();
      }
      public removeConstraint_result getResult(I iface, removeConstraint_args args) throws org.apache.thrift.TException {
        removeConstraint_result result = new removeConstraint_result();
        try {
          iface.removeConstraint(args.login, args.tableName, args.constraint);
        } catch (AccumuloException ouch1) {
          result.ouch1 = ouch1;
        } catch (AccumuloSecurityException ouch2) {
          result.ouch2 = ouch2;
        } catch (TableNotFoundException ouch3) {
          result.ouch3 = ouch3;
        }
        return result;
      }
      public void read(org.apache.thrift.protocol.TProtocol prot, removeConstraint_result struct) throws org.apache.thrift.TException {
        TTupleProtocol iprot = (TTupleProtocol) prot;
        BitSet incoming = iprot.readBitSet(3);
        if (incoming.get(0)) {
          struct.ouch1 = new AccumuloException();
          struct.ouch1.read(iprot);
          struct.setOuch1IsSet(true);
        }
        if (incoming.get(1)) {
          struct.ouch2 = new AccumuloSecurityException();
          struct.ouch2.read(iprot);
          struct.setOuch2IsSet(true);
        }
        if (incoming.get(2)) {
          struct.ouch3 = new TableNotFoundException();
          struct.ouch3.read(iprot);
          struct.setOuch3IsSet(true);
        }
      }
    public int compareTo(removeConstraint_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;
      removeConstraint_result typedOther = (removeConstraint_result)other;

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
      lastComparison = Boolean.valueOf(isSetOuch3()).compareTo(typedOther.isSetOuch3());
      if (lastComparison != 0) {
        return lastComparison;
      }
      if (isSetOuch3()) {
        lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ouch3, typedOther.ouch3);
        if (lastComparison != 0) {
          return lastComparison;
        }
      }
      return 0;
    }
    public Object getFieldValue(_Fields field) {
      switch (field) {
      case OUCH1:
        return getOuch1();

      case OUCH2:
        return getOuch2();

      case OUCH3:
        return getOuch3();

      }
      throw new IllegalStateException();
    }
