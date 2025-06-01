	private static CtStatement internalCompileStatement(CtElement st, CtTypeReference returnType) {
		Factory f = st.getFactory();

		String contents = createWrapperContent(st, f, returnType);

		build(f, contents);

		CtType<?> c = f.Type().get(WRAPPER_CLASS_NAME);

		// Get the part we want

		CtMethod<?> wrapper = c.getMethod(WRAPPER_METHOD_NAME);

		List<CtStatement> statements = wrapper.getBody().getStatements();
		CtStatement ret = statements.get(statements.size() - 1);

		// Clean up
		c.getPackage().removeType(c);

		//disconnect element from the parent, so it can be added to another model
		ret.delete();
		ret.setParent(null);

		if (ret instanceof CtClass) {
			CtClass klass = (CtClass) ret;
			ret.getFactory().Package().getRootPackage().addType(klass);
			klass.setSimpleName(klass.getSimpleName().replaceAll("^[0-9]*", ""));
		}
		return ret;
	}
	public static <T> CtExpression<T> compileExpression(
			CtCodeSnippetExpression<T> expr) throws SnippetCompilationError {

		CtReturn<T> ret = (CtReturn<T>) internalCompileStatement(expr, expr.getFactory().Type().OBJECT);

		CtExpression<T> returnedExpression = ret.getReturnedExpression();

		// this compiled expression is not in a parent, ready to be used
		returnedExpression.delete();
		returnedExpression.setParent(null);

		return returnedExpression;
	}
