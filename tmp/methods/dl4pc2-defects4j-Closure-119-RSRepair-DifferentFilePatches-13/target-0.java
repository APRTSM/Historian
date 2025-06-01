  private boolean isTypedef(Ref ref) {
    // If this is an annotated EXPR-GET, don't do anything.
    Node parent = ref.node.getParent();
    if (parent.isExprResult()) {
      JSDocInfo info = ref.node.getJSDocInfo();
      if (info != null && info.hasTypedefType()) {
        return true;
      }
    }
    StringBuilder builder = new StringBuilder();
	return false;
  }
  private void reportRefToUndefinedName(Name name, Ref ref) {
    // grab the highest undefined ancestor to output in the warning message.
    while (name.parent != null &&
           name.parent.globalSets + name.parent.localSets == 0) {
      name = name.parent;
    }
  }
