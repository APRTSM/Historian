    public void releaseSub() {
        byte[] footer = layout.getFooter();
        if (footer != null) {
            write(footer);
        }
        close();
    }
    private boolean rollover(final RolloverStrategy strategy) {

        try {
            // Block until the asynchronous operation is completed.
            semaphore.acquire();
        } catch (final InterruptedException ie) {
            LOGGER.error("Thread interrupted while attempting to check rollover", ie);
            return false;
        }

        boolean success = false;
        Thread thread = null;

        try {
            final RolloverDescription descriptor = strategy.rollover(this);
            if (descriptor != null) {
                close();
                if (descriptor.getSynchronous() != null) {
                    LOGGER.debug("RollingFileManager executing synchronous {}", descriptor.getSynchronous());
                    try {
                        success = descriptor.getSynchronous().execute();
                    } catch (final Exception ex) {
                        LOGGER.error("Error in synchronous task", ex);
                    }
                }

                if (success && descriptor.getAsynchronous() != null) {
                    LOGGER.debug("RollingFileManager executing async {}", descriptor.getAsynchronous());
                    thread = new Thread(new AsyncAction(descriptor.getAsynchronous(), this));
                    thread.start();
                }
                return true;
            }
            return false;
        } finally {
            if (thread == null) {
                semaphore.release();
            }
        }

    }
    public RollingRandomAccessFileManager(final RandomAccessFile raf, final String fileName,
            final String pattern, final OutputStream os, final boolean append,
            final boolean immediateFlush, final int bufferSize, final long size, final long time,
            final TriggeringPolicy policy, final RolloverStrategy strategy,
            final String advertiseURI, final Layout<? extends Serializable> layout) {
        super(fileName, pattern, os, append, size, time, policy, strategy, advertiseURI, layout, bufferSize);
        this.isImmediateFlush = immediateFlush;
        this.randomAccessFile = raf;
        isEndOfBatch.set(Boolean.FALSE);
        this.buffer = ByteBuffer.allocate(bufferSize);
    }
    protected void createFileAfterRollover() throws IOException {
        this.randomAccessFile = new RandomAccessFile(getFileName(), "rw");
        if (isAppend()) {
            randomAccessFile.seek(randomAccessFile.length());
        }
    }
