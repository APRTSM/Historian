  public ReverseAbstractInterpreter getReverseAbstractInterpreter() {
    if (abstractInterpreter == null) {
      ChainableReverseAbstractInterpreter interpreter =
          new SemanticReverseAbstractInterpreter(
              getCodingConvention(), getTypeRegistry());
      if (options.closurePass) {
      }
      abstractInterpreter = interpreter;
    }
    return abstractInterpreter;
  }
    protected void add(CheckLevel level, String groupName) {
    }
