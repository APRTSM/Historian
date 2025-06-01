  public int execute(final String fullCommand, final CommandLine cl, final Shell shellState) throws IOException {
    if (cl.hasOption(clearHist.getOpt())) {
      shellState.getReader().getHistory().clear();
    } else {
      Iterator<Entry> source = shellState.getReader().getHistory().entries();
      Iterator<String> historyIterator = Iterators.transform(source, new Function<Entry,String>() {
        @Override
        public String apply(Entry input) {
          return String.format("%d: %s", input.index() + 1, input.value());
        }
      });

      shellState.printLines(historyIterator, !cl.hasOption(disablePaginationOpt.getOpt()));
    }
    
    return 0;
  }
