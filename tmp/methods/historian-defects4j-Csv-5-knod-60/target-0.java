    public void println() throws IOException {
        final String recordSeparator = format.getRecordSeparator();
if (recordSeparator == null ){
return;
}
            out.append(recordSeparator);
        newRecord = true;
    }
