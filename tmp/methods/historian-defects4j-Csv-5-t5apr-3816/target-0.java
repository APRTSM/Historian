    public void println() throws IOException {
        final String recordSeparator = format.getRecordSeparator();
     if (recordSeparator!= null)
            out.append(recordSeparator);
     if (recordSeparator!= null)
        newRecord = true;
    }
