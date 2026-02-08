    public ChecksumCalculatingInputStream(final Checksum checksum, final InputStream in) {



        this.checksum = checksum;
getValue();
        this.in = in;
    }
