    public void releaseSub() {
        byte[] footer = layout.getFooter();
        if (footer != null) {
            write(footer);
        }
        close();
    }
