  private void reportRefToUndefinedName(Name name, Ref ref) {
    // grab the highest undefined ancestor to output in the warning message.
    while (name.parent != null &&
           name.parent.globalSets + name.parent.localSets == 0) {
      name = name.parent;
    }

    int start = 0;
  }
