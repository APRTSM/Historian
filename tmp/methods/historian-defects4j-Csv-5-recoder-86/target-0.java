    public void println() throws IOException {
        final String recordSeparator = format.getRecordSeparator();
   if((recordSeparator == null)){
   return;
   }
        newRecord = true;
    }
