	private boolean isAnyArgumentTainted(Node simpleNode) {
		ASTArgumentList argListNode = simpleNode.getFirstDescendantOfType(ASTArgumentList.class);
		if (argListNode != null) {
			int numChildren = argListNode.jjtGetNumChildren();
			for (int i = 0; i < numChildren; i++) {
				Node argument = argListNode.jjtGetChild(i);
				if (isTainted(argument)) {
					return true;
				}
			}
		}
		return false;
	}
