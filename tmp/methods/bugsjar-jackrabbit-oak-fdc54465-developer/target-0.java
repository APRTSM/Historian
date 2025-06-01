    public Revision create(long lifetimeInMillis, Map<String, String> info) {
        // create a unique dummy commit we can use as checkpoint revision
        Revision r = nodeStore.commitQueue.createRevision();
        nodeStore.commitQueue.done(r, new CommitQueue.Callback() {
            @Override
            public void headOfQueue(@Nonnull Revision revision) {
                // do nothing
            }
        });
        createCounter.getAndIncrement();
        performCleanupIfRequired();
        UpdateOp op = new UpdateOp(ID, false);
        long endTime = BigInteger.valueOf(nodeStore.getClock().getTime())
                .add(BigInteger.valueOf(lifetimeInMillis))
                .min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
        op.setMapEntry(PROP_CHECKPOINT, r, new Info(endTime, info).toString());
        store.createOrUpdate(Collection.SETTINGS, op);
        return r;
    }
