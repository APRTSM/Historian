    public Revision create(long lifetimeInMillis, Map<String, String> info) {
        Revision r = nodeStore.getHeadRevision();
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
