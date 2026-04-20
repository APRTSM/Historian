    public void println() throws IOException {
        final String recordSeparator = format.getRecordSeparator();
     if (format.getRecordSeparator() == null) { return; }
            out.append(recordSeparator);
     if (format.getRecordSeparator() == null) { return; }
        newRecord = true;
    }
