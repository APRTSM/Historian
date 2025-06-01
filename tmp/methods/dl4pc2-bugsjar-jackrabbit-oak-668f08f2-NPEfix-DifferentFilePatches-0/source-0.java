    public void initialize() throws Exception {
        if (initialized) {
            throw new IllegalStateException("already initialized");
        }

        cache = Collections.synchronizedMap(SimpleLRUCache.<Id, Object>newInstance(determineInitialCacheSize()));

        // make sure we've got a HEAD commit
        head = pm.readHead();
        if (head == null || head.getBytes().length == 0) {
            // assume virgin repository
            byte[] rawHead = longToBytes(++headCounter);
            head = new Id(rawHead);
            
            Id rootNodeId = pm.writeNode(new MutableNode(this, "/"));
            MutableCommit initialCommit = new MutableCommit();
            initialCommit.setCommitTS(System.currentTimeMillis());
            initialCommit.setRootNodeId(rootNodeId);
            pm.writeCommit(head, initialCommit);
            pm.writeHead(head);
        } else {
            headCounter = Long.parseLong(head.toString(), 16);
        }

        initialized = true;
    }
