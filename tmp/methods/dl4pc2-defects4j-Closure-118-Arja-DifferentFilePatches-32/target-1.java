  void expectIndexMatch(NodeTraversal t, Node n, JSType objType,
                        JSType indexType) {
    Preconditions.checkState(n.isGetElem());
    Node indexNode = n.getLastChild();
    if (objType.isStruct()) {
      report(JSError.make(t.getSourceName(), indexNode,
                          ILLEGAL_PROPERTY_ACCESS, "'[]'", "struct"));
    }
    if (objType.isUnknownType()) {
      expectStringOrNumber(t, indexNode, indexType, "property access");
    } else {
      ObjectType dereferenced = objType.dereference();
      if (dereferenced != null && dereferenced
          .getTemplateTypeMap()
          .hasTemplateKey(typeRegistry.getObjectIndexKey())) {
        expectCanAssignTo(t, indexNode, indexType, dereferenced
            .getTemplateTypeMap().getTemplateType(typeRegistry.getObjectIndexKey()),
            "restricted index type");
      } else if (dereferenced != null && dereferenced.isArrayType()) {
        expectNumber(t, indexNode, indexType, "array access");
      } else if (objType.matchesObjectContext()) {
      } else {
        mismatch(t, n, "only arrays or objects can be accessed",
            objType,
            typeRegistry.createUnionType(ARRAY_TYPE, OBJECT_TYPE));
      }
    }
  }
  public String checkTreeEquals(Node node2) {
      NodeMismatch diff = checkTreeEqualsImpl(node2);
      if (diff != null) {
        parent = null;
		return "Node tree inequality:" +
            "\nTree1:\n" + toStringTree() +
            "\n\nTree2:\n" + node2.toStringTree() +
            "\n\nSubtree1: " + diff.nodeA.toStringTree() +
            "\n\nSubtree2: " + diff.nodeB.toStringTree();
      }
      return null;
  }
