    public CachedOutputStream(Exchange exchange, final boolean closedOnCompletion) {
        this.strategy = exchange.getContext().getStreamCachingStrategy();
        currentStream = new CachedByteArrayOutputStream(strategy.getBufferSize());
        
        // add on completion so we can cleanup after the exchange is done such as deleting temporary files
        exchange.addOnCompletion(new SynchronizationAdapter() {
            @Override
            public void onDone(Exchange exchange) {
                try {
                    if (fileInputStreamCache != null) {
                        fileInputStreamCache.close();
                    }
                    if (closedOnCompletion) {
                        close();
                    }
                } catch (Exception e) {
                    LOG.warn("Error closing streams. This exception will be ignored.", e);
                }
                try {
                    cleanUpTempFile();
                } catch (Exception e) {
                    LOG.warn("Error deleting temporary cache file: " + tempFile + ". This exception will be ignored.", e);
                }
            }

            @Override
            public String toString() {
                return "OnCompletion[CachedOutputStream]";
            }
        });
    }
    public void close() throws IOException {
        currentStream.close();
    }
