  public ReverseAbstractInterpreter getReverseAbstractInterpreter() {
    if (abstractInterpreter == null) {
      ChainableReverseAbstractInterpreter interpreter =
          new SemanticReverseAbstractInterpreter(
              getCodingConvention(), getTypeRegistry());
      if (options.closurePass) {
        interpreter = new ClosureReverseAbstractInterpreter(
            getCodingConvention(), getTypeRegistry())
            .append(interpreter).getFirst();
      }
      abstractInterpreter = interpreter;
    }
    return abstractInterpreter;
  }
  private boolean shouldReportThis(Node n, Node parent) {
    if (assignLhsChild != null) {
      // Always report a THIS on the left side of an assign.
      return true;
    }

    // Also report a THIS with a property access.
    return parent != null && NodeUtil.isGet(parent);
  }
