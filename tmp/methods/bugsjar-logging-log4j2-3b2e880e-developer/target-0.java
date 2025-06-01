    protected void commitAndClose() {
        // all NoSQL drivers auto-commit (since NoSQL doesn't generally use the concept of transactions).
        // also, all our NoSQL drivers use internal connection pooling and provide clients, not connections.
        // thus, we should not be closing the client until shutdown as NoSQL is very different from SQL.
        // see LOG4J2-591 and LOG4J2-676
    }
    protected void shutdownInternal() {
        // NoSQL doesn't use transactions, so all we need to do here is simply close the client
        Closer.closeSilent(this.connection);
    }
