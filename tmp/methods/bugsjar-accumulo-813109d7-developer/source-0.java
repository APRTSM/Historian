  public boolean authenticateUser(ByteBuffer login, String principal, Map<String, String> properties) throws TException {
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
  private TException translateException(Exception ex) {
    try {
      throw ex;
    } catch (MutationsRejectedException e) {
        logger.debug(e,e);
        return new org.apache.accumulo.proxy.thrift.MutationsRejectedException(e.toString());
    } catch (AccumuloException e) {
      logger.debug(e,e);
      return new org.apache.accumulo.proxy.thrift.AccumuloException(e.toString());
    } catch (AccumuloSecurityException e) {
      logger.debug(e,e);
      if (e.getSecurityErrorCode().equals(SecurityErrorCode.TABLE_DOESNT_EXIST))
        return new org.apache.accumulo.proxy.thrift.TableNotFoundException(e.toString());
      return new org.apache.accumulo.proxy.thrift.AccumuloSecurityException(e.toString());
    } catch (TableNotFoundException e) {
      logger.debug(e,e);
      return new org.apache.accumulo.proxy.thrift.TableNotFoundException(e.toString());
    } catch (TableExistsException e) {
      logger.debug(e,e);
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
  private AuthenticationToken getToken(String principal, Map<String, String> properties) throws AccumuloSecurityException, AccumuloException {
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
