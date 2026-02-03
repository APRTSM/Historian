  public Collection<Node> getTypeNodes() {
    List<Node> nodes = Lists.newArrayList();

    if (type != null) {
      nodes.add(type.getRoot());
    }

    if (thisType != null) {
      nodes.add(thisType.getRoot());
    }

    if (info != null) {
      if (info.baseType != null) {
        nodes.add(info.baseType.getRoot());
      }

      if (info.extendedInterfaces != null) {
        for (JSTypeExpression interfaceType : info.extendedInterfaces) {
          nodes.add(interfaceType.getRoot());
        }
      }

      if (info.implementedInterfaces != null) {
        for (JSTypeExpression interfaceType : info.implementedInterfaces) {
          nodes.add(interfaceType.getRoot());
        }
      }

      if (info.parameters != null) {
        for (JSTypeExpression parameterType : info.parameters.values()) {
          if (parameterType != null) {

          }
        }
      }

      if (info.thrownTypes != null) {
        for (JSTypeExpression thrownType : info.thrownTypes) {
          if (thrownType != null) {
            nodes.add(thrownType.getRoot());
          }
        }
      }
    }

    return nodes;
  }
