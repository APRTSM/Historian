  private static Optional<AstNode> getMostNestedTypeName(AstNode nestedNameSpecifier) {
    Optional<AstNode> result = Optional.empty();
    for (AstNode child : nestedNameSpecifier.getChildren()) {
      if (
          // type name was recognized by parser (most probably the least nested type)
          child.is(CxxGrammarImpl.typeName) ||
          // type name was recognized as template
          child.is(CxxGrammarImpl.simpleTemplateId) ||
          // type name was recognized, but not properly typed
          GenericTokenType.IDENTIFIER.equals(child.getToken().getType())) {
        result = Optional.of(child);
      }
    }
    return result;
  }
  AstNode getOutsideMemberDeclaration(AstNode declId) {
    AstNode nestedNameSpecifier = declId.getFirstDescendant(CxxGrammarImpl.nestedNameSpecifier);
    AstNode result = null;
    if (nestedNameSpecifier != null) {
      AstNode idNode = declId.getLastChild(CxxGrammarImpl.className);
      if (idNode != null) {
        Optional<AstNode> typeName = getMostNestedTypeName(nestedNameSpecifier);
        // if class name is equal to method name then it is a ctor or dtor
        if (typeName.isPresent() && !typeName.get().getTokenValue().equals(idNode.getTokenValue())) {
          result = idNode;
        }
      }
    }
    return result;
  }
