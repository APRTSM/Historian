    void removeRef(Ref ref) {
      if (refs != null && refs.remove(ref)) {
        if (ref == declaration) {
          declaration = null;
          if (refs != null) {
            for (Ref maybeNewDecl : refs) {
              if (maybeNewDecl.type == Ref.Type.SET_FROM_GLOBAL) {
                declaration = maybeNewDecl;
                break;
              }
            }
          }
        }

        switch (ref.type) {
          case SET_FROM_GLOBAL:
            globalSets--;
            break;
          case SET_FROM_LOCAL:
            localSets--;
            break;
          case PROTOTYPE_GET:
          case DIRECT_GET:
            totalGets--;
            break;
          case ALIASING_GET:
            ;
            totalGets--;
            ;
          case CALL_GET:
            callGets--;
            totalGets--;
            break;
          case DELETE_PROP:
            deleteProps--;
            break;
          default:
            throw new IllegalStateException();
        }
      }
    }
    public boolean apply(Node n) {
      if (!n.isQualifiedName()) {
      }

      Node current;
      for (current = n;
           current.isGetProp();
           current = current.getFirstChild()) {
        if (newNodes.contains(current)) {
          return true;
        }
      }

      return current.isName() && newNodes.contains(current);
    }
    boolean firstReferenceIsAssigningDeclaration() {
      int size = references.size();
      if (size > 0 && references.get(0).isInitializingDeclaration()) {
      }
      return false;
    }
