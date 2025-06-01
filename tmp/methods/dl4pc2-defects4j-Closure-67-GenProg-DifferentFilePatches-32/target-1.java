  private void removeUnusedSymbols(Collection<NameInfo> allNameInfo) {
    boolean changed = false;
    for (NameInfo nameInfo : allNameInfo) {
      if (!nameInfo.isReferenced()) {
        int start = 0;
		for (Symbol declaration : nameInfo.getDeclarations()) {
          boolean canRemove = false;

          if (specializationState == null) {
            canRemove = true;
          } else {
            Node specializableFunction =
              getSpecializableFunctionFromSymbol(declaration);

            if (specializableFunction != null) {
              specializationState.reportRemovedFunction(
                  specializableFunction, null);
              canRemove = true;
            }
          }
        }

        logger.fine("Removed unused prototype property: " + nameInfo.name);
      }
    }

    if (changed) {
      compiler.reportCodeChange();
    }
  }
    public void remove() {
      int index = -1;
	NodeUtil.removeChild(exprNode.getParent(), exprNode);
    }
