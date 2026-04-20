    public void println() throws IOException {
        final String recordSeparator = format.getRecordSeparator();
     if (recordSeparator == null || recordSeparator.length() == 0) return;
            out.append(recordSeparator);
     if (recordSeparator == null || recordSeparator.length() == 0) return;
        newRecord = true;
    }
