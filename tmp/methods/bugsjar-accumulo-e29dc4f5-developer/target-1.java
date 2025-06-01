    public void onRemoval(RemovalNotification<UUID,BatchWriterPlusException> notification) {
      try {
        BatchWriterPlusException value = notification.getValue();
        if (value.exception != null)
          throw value.exception;
        notification.getValue().writer.close();
      } catch (MutationsRejectedException e) {
        logger.warn(e, e);
      }
    }
  private void addCellsToWriter(Map<ByteBuffer,List<ColumnUpdate>> cells, BatchWriterPlusException bwpe) throws MutationsRejectedException {
    if (bwpe.exception != null)
      return;

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
      try {
        bwpe.writer.addMutation(m);
      } catch (MutationsRejectedException mre) {
        bwpe.exception = mre;
      }
    }
  }
  public void updateAndFlush(ByteBuffer login, String tableName, Map<ByteBuffer,List<ColumnUpdate>> cells) throws TException {
    try {
      BatchWriterPlusException bwpe = getWriter(login, tableName, null);
      addCellsToWriter(cells, bwpe);
      if (bwpe.exception != null)
        throw bwpe.exception;
      bwpe.writer.flush();
      bwpe.writer.close();
    } catch (Exception e) {
      throw translateException(e);
    }
  }
  public void update(String writer, Map<ByteBuffer,List<ColumnUpdate>> cells) throws TException {
    try {
      BatchWriterPlusException bwpe = writerCache.getIfPresent(UUID.fromString(writer));
      if (bwpe == null) {
        throw new UnknownWriter("Writer never existed or no longer exists");
      }
      addCellsToWriter(cells, bwpe);
    } catch (Exception e) {
      throw translateException(e);
    }
  }
  public void closeWriter(String writer) throws TException {
    try {
      BatchWriterPlusException bwpe = writerCache.getIfPresent(UUID.fromString(writer));
      if (bwpe == null) {
        throw new UnknownWriter("Writer never existed or no longer exists");
      }
      if (bwpe.exception != null)
        throw bwpe.exception;
      bwpe.writer.close();
      writerCache.invalidate(UUID.fromString(writer));
    } catch (Exception e) {
      throw translateException(e);
    }
  }
  public String createWriter(ByteBuffer login, String tableName, WriterOptions opts) throws TException {
    try {
      BatchWriterPlusException writer = getWriter(login, tableName, opts);
      UUID uuid = UUID.randomUUID();
      writerCache.put(uuid, writer);
      return uuid.toString();
    } catch (Exception e) {
      throw translateException(e);
    }
  }
  private BatchWriterPlusException getWriter(ByteBuffer login, String tableName, WriterOptions opts) throws Exception {
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
    BatchWriterPlusException result = new BatchWriterPlusException();
    result.writer = getConnector(login).createBatchWriter(tableName, cfg);
    return result;
  }
  public void flush(String writer) throws TException {
    try {
      BatchWriterPlusException bwpe = writerCache.getIfPresent(UUID.fromString(writer));
      if (bwpe == null) {
        throw new UnknownWriter("Writer never existed or no longer exists");
      }
      if (bwpe.exception != null)
        throw bwpe.exception;
      bwpe.writer.flush();
    } catch (Exception e) {
      throw translateException(e);
    }
  }
      public void getResult() throws org.apache.thrift.TException {
        if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
          throw new IllegalStateException("Method call not finished!");
        }
        org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
        org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
      }
    public void update(String writer, Map<ByteBuffer,List<ColumnUpdate>> cells) throws org.apache.thrift.TException
    {
      send_update(writer, cells);
    }
      protected boolean isOneway() {
        return true;
      }
      public org.apache.thrift.TBase getResult(I iface, update_args args) throws org.apache.thrift.TException {
        iface.update(args.writer, args.cells);
        return null;
      }
