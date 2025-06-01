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
