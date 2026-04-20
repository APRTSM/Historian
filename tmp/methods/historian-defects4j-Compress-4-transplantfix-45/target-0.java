    public void close() throws IOException {
        def.finish();

        if (raf != null) {
            raf.close();
        }
        if (out != null) {
            out.close();
        }
    }
