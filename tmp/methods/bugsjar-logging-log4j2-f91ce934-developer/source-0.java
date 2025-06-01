    protected void setOutputStream(final OutputStream os) {
        this.os = os;
    }
    public synchronized void setHeader(final byte[] header) {
        if (header != null) {
            try {
                this.os.write(header, 0, header.length);
            } catch (final IOException ioe) {
                LOGGER.error("Unable to write header", ioe);
            }
        }
    }
