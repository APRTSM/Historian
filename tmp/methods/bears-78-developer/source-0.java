	private <T> void printCtFieldAccess(CtFieldAccess<T> f) {
		enterCtExpression(f);
		try (Writable _context = context.modify()) {
			if (f.getVariable().isStatic() && f.getTarget() instanceof CtTypeAccess) {
				_context.ignoreGenerics(true);
			}
			CtExpression<?> target = f.getTarget();
			if (target != null) {
				boolean isInitializeStaticFinalField = isInitializeStaticFinalField(f.getTarget());
				boolean isStaticField = f.getVariable().isStatic();
				boolean isImportedField = importsContext.isImported(f.getVariable());

				if (!isInitializeStaticFinalField && !(isStaticField && isImportedField)) {
					if (target.isImplicit()) {
						/*
						 * target is implicit, check whether there is no conflict with an local variable, catch variable or parameter
						 * in case of conflict make it explicit, otherwise the field access is shadowed by that variable.
						 * Search for potential variable declaration until we found a class which declares or inherits this field
						 */
						final CtField<?> field = f.getVariable().getFieldDeclaration();
						final String fieldName = field.getSimpleName();
						CtVariable<?> var = f.getVariable().map(new PotentialVariableDeclarationFunction(fieldName)).first();
						if (var != field) {
							//another variable declaration was found which is hiding the field declaration for this field access. Make the field access expicit
							target.setImplicit(false);
						}
					}
					printer.snapshotLength();
					scan(target);
					if (printer.hasNewContent()) {
						printer.write(".");
					}
				}
				_context.ignoreStaticAccess(true);
			}
			scan(f.getVariable());
		}
		exitCtExpression(f);
	}
