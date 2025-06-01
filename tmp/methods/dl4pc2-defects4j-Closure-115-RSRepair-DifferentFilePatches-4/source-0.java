  private boolean mimimizeCost(FunctionState fs) {
    if (!inliningLowersCost(fs)) {
      // Try again without Block inlining references
      if (fs.hasBlockInliningReferences()) {
        fs.setRemove(false);
        fs.removeBlockInliningReferences();
        if (!fs.hasReferences() || !inliningLowersCost(fs)) {
          return false;
        }
      } else {
        return false;
      }
    }
    return true;
  }
