    public void releaseSub() {
        writeFooter();
        close();
    }
    protected void writeFooter() {
        if (layout == null) {
            return;
        }
        byte[] footer = layout.getFooter();
        if (footer != null) {
            write(footer);
        }
    }
