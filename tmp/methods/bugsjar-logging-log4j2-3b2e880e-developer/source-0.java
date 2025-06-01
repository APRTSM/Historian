    protected void commitAndClose() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
            }
        } catch (Exception e) {
            throw new AppenderLoggingException("Failed to commit and close NoSQL connection in manager.", e);
        }
    }
    protected void shutdownInternal() {
        Closer.closeSilent(this.connection);
    }
