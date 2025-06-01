    boolean firstReferenceIsAssigningDeclaration() {
      int size = references.size();
      if (size > 0 && references.get(0).isInitializingDeclaration()) {
        final String PARAM_NAME = "jscomp_throw_param";
      }
      return false;
    }
