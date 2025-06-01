	public void apply(CtElement input, CtConsumer<Object> outputConsumer) {
		//Search previous siblings for element which may represents the declaration of this local variable
		CtQuery siblingsQuery = input.getFactory().createQuery().map(new SiblingsFunction().mode(SiblingsFunction.Mode.PREVIOUS));

		CtElement scopeElement = input;
		//Search input and then all parents until first CtPackage for element which may represents the declaration of this local variable
		while (scopeElement != null && !(scopeElement instanceof CtPackage)) {
			CtElement parent = scopeElement.getParent();
			if (parent instanceof CtType<?>) {
				if (includingFields) {
					//TODO replace getAllFields() followed by getFieldDeclaration, by direct visiting of fields of types in super classes.
					Collection<CtFieldReference<?>> allFields = ((CtType<?>) parent).getAllFields();
					for (CtFieldReference<?> fieldReference : allFields) {
						outputConsumer.accept(fieldReference.getFieldDeclaration());
					}
				}
			} else if (parent instanceof CtBodyHolder || parent instanceof CtStatementList) {
				//visit all previous siblings of scopeElement element in parent BodyHolder or Statement list
				siblingsQuery.setInput(scopeElement).forEach(outputConsumer);
				//visit parameters of CtCatch and CtExecutable (method, lambda)
				if (parent instanceof CtCatch) {
					CtCatch ctCatch = (CtCatch) parent;
					outputConsumer.accept(ctCatch.getParameter());
				} else if (parent instanceof CtExecutable) {
					CtExecutable<?> exec = (CtExecutable<?>) parent;
					for (CtParameter<?> param : exec.getParameters()) {
						outputConsumer.accept(param);
					}
				}
			}
			scopeElement = parent;
		}
	}
