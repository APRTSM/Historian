    public StandbyClient(String host, int port, SegmentStore store) throws SSLException {
        this(host, port, store, false, 10000);
    }
