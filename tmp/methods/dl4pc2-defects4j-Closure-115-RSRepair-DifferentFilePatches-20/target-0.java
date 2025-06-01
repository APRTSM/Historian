    void maybeAddReference(NodeTraversal t, FunctionState fs,
        Node callNode, JSModule module) {
      if (!fs.canInline()) {
        return;
      }

      boolean referenceAdded = false;
      InliningMode mode = fs.canInlineDirectly()
           ? InliningMode.DIRECT : InliningMode.BLOCK;
      referenceAdded = maybeAddReferenceUsingMode(
          t, fs, callNode, module, mode);
      if (!referenceAdded &&
          mode == InliningMode.DIRECT && blockFunctionInliningEnabled) {
        // This reference can not be directly inlined, see if
        // block replacement inlining is possible.
        mode = InliningMode.BLOCK;
      }

      if (!referenceAdded) {
        // Don't try to remove a function if we can't inline all
        // the references.
        fs.setRemove(false);
      }
    }
