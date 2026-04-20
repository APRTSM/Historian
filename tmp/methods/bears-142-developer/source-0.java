  public void notify(DelegateExecution execution) {
    Context.getCommandContext().getHistoryManager()
      .recordActivityEnd((ExecutionEntity) execution);
  }
