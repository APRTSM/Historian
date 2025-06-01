    public void initialize() throws Exception {
        if (initialized) {
            throw new IllegalStateException("already initialized");
        }

        initialCacheSize = determineInitialCacheSize();
        
        cache = CacheBuilder.newBuilder()
                .maximumWeight(initialCacheSize)
                .weigher(new Weigher<Id, CacheObject>() {
                    public int weigh(Id id, CacheObject obj) {
                        return obj.getMemory();
                    }
                })
                .build();

        // make sure we've got a HEAD commit
        Id[] ids = pm.readIds();
        head = ids[0];
        if (head == null || head.getBytes().length == 0) {
            // assume virgin repository
            byte[] rawHead = Id.fromLong(commitCounter.incrementAndGet())
                    .getBytes();
            head = new Id(rawHead);

            Id rootNodeId = pm.writeNode(new MutableNode(this));
            MutableCommit initialCommit = new MutableCommit();
            initialCommit.setCommitTS(System.currentTimeMillis());
            pm.writeHead(head);
        } else {
            Id lastCommitId = head;
            if (ids[1] != null && ids[1].compareTo(lastCommitId) > 0) {
                lastCommitId = ids[1];
            }
            commitCounter.set(Long.parseLong(lastCommitId.toString(), 16));
        }

        if (gcpm != null) {
            gcExecutor = Executors.newScheduledThreadPool(1,
                    new ThreadFactory() {
                        @Override
                        public Thread newThread(Runnable r) {
                            return new Thread(r, "RevisionStore-GC");
                        }
                    });
            gcExecutor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    if (cache.size() >= initialCacheSize) {
                        gc();
                    }
                }
            }, 60, 1, TimeUnit.MINUTES); // TODO: Should start earlier
        }

        initialized = true;
    }
        public boolean equals(Object obj) {
            if (obj instanceof PutTokenImpl) {
            }
            return super.equals(obj);
        }
