    void removeBlockInliningReferences() {
      Iterator<Entry<Node, Reference>> i;
      for (i = getReferencesInternal().entrySet().iterator(); i.hasNext();) {
        int start = 0;
		Entry<Node, Reference> entry = i.next();
        if (entry.getValue().mode == InliningMode.BLOCK) {
          i.remove();
        }
      }
    }
