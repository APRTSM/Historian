    private void handleObjectLit(NodeTraversal t, Node n) {
      for (Node child = n.getFirstChild();
          child != null;
          child = child.getNext()) {
        // Maybe STRING, GET, SET

        // We should never see a mix of numbers and strings.
        String name = child.getString();
        T type = typeSystem.getType(getScope(), n, name);

        Property prop = getProperty(name);
        if (!prop.scheduleRenaming(child,
                                   processProperty(t, prop, type, null))) {
          // TODO(user): It doesn't look like the user can do much in this
          // case right now.
          if (propertiesToErrorFor.containsKey(name)) {
            compiler.report(JSError.make(
                t.getSourceName(), child, propertiesToErrorFor.get(name),
                Warnings.INVALIDATION, name,
                (type == null ? "null" : type.toString()), n.toString(), ""));
          }
        }
      }
    }
  NodeMismatch checkTreeEqualsImpl(Node node2) {
    if (!isEquivalentTo(node2, false, false, false)) {
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
  public boolean resetImplicitPrototype(
      JSType type, ObjectType newImplicitProto) {
    if (type instanceof PrototypeObjectType) {
      PrototypeObjectType poType = (PrototypeObjectType) type;
      poType.clearCachedValues();
      poType.setImplicitPrototype(newImplicitProto);
      return true;
    }
    return false;
  }
