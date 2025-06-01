    public synchronized void setHeader(final byte[] header) {
        if (header != null) {
            this.header = header;
            try {
                this.os.write(header, 0, header.length);
            } catch (final IOException ioe) {
                LOGGER.error("Unable to write header", ioe);
            }
        }
    }
    protected void setOutputStream(final OutputStream os) {
        this.os = os;
        if (header != null) {
            try {
                this.os.write(header, 0, header.length);
            } catch (final IOException ioe) {
                LOGGER.error("Unable to write header", ioe);
            }
        }
    }
