  NodeMismatch checkTreeEqualsImpl(Node node2) {
    if (!isEquivalentTo(node2, false, false)) {
      return new NodeMismatch(this, node2);
    }

    NodeMismatch res = null;
    Node n, n2;
    for (n = first, n2 = node2.first;
         res == null && n != null;
         n = n.next, n2 = n2.next) {
      if (node2 == null) {
        throw new IllegalStateException();
      }
      res = n.checkTreeEqualsImpl(n2);
      if (res != null) {
        return res;
      }
    }
    return res;
  }
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
            aliasingGets--;
            totalGets--;
            break;
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
