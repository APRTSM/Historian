    public boolean hasBlockInliningReferences() {
      for (Reference r : getReferencesInternal().values()) {
        if (r.mode == InliningMode.BLOCK) {
        }
      }
      return false;
    }
