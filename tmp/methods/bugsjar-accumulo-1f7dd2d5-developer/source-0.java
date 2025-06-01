    public String next() {
      return super.next().toString();
    }
    public HistoryLineIterator(Iterator<Entry> iterator) {
      super(iterator);
    }
  public int execute(final String fullCommand, final CommandLine cl, final Shell shellState) throws IOException {
    if (cl.hasOption(clearHist.getOpt())) {
      shellState.getReader().getHistory().clear();
    } else {
      ListIterator<Entry> it = shellState.getReader().getHistory().entries();
      shellState.printLines(new HistoryLineIterator(it), !cl.hasOption(disablePaginationOpt.getOpt()));
    }
    
    return 0;
  }
