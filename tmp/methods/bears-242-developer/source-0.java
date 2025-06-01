  AstNode getOutsideMemberDeclaration(AstNode declId) {
    AstNode nestedNameSpecifier = declId.getFirstDescendant(CxxGrammarImpl.nestedNameSpecifier);
    AstNode result = null;
    if (nestedNameSpecifier != null) {
      AstNode idNode = declId.getLastChild(CxxGrammarImpl.className);
      if (idNode != null) {
        AstNode className = nestedNameSpecifier.getFirstDescendant(CxxGrammarImpl.className);
        // if class name is equal to method name then it is a ctor or dtor
        if ((className != null) && !className.getTokenValue().equals(idNode.getTokenValue())) {
          result = idNode;
        }
      }
    }
    return result;
  }
