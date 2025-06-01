	private boolean isAnyArgumentTainted(Node simpleNode) {
		ASTArgumentList argListNode = simpleNode.getFirstDescendantOfType(ASTArgumentList.class);
		if (argListNode != null) {
			if (isSanitized(argListNode)) {
				return false;
			}
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
	private boolean isSanitized(ASTArgumentList argListNode) {
		try {
			ASTArguments arguments = (ASTArguments) argListNode.jjtGetParent();
			ASTPrimarySuffix suffix = (ASTPrimarySuffix) arguments.jjtGetParent();
			ASTPrimaryExpression exp = (ASTPrimaryExpression) suffix.jjtGetParent();
			String method = getMethod(exp);
			Class<?> type = getJavaType(exp);
			String typeName = type.getName();
			if (isSink(typeName, method)) {
				return false;
			}
			if (isSanitizerMethod(typeName, method) || isSafeType(getReturnType(exp, typeName, method))) {
				return true;
			}
		}
		catch (Exception e) {
			return false;
		}
		return false;
	}
