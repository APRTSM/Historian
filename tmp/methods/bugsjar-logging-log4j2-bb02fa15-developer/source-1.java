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
