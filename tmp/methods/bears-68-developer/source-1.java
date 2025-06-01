	public <T> void visitCtInvocation(CtInvocation<T> invocation) {
		enterCtStatement(invocation);
		enterCtExpression(invocation);
		if (invocation.getExecutable().isConstructor()) {
			// It's a constructor (super or this)
			elementPrinterHelper.writeActualTypeArguments(invocation.getExecutable());
			CtType<?> parentType;
			try {
				parentType = invocation.getParent(CtType.class);
			} catch (ParentNotInitializedException e) {
				parentType = null;
			}
			if (parentType != null && parentType.getQualifiedName() != null && parentType.getQualifiedName().equals(invocation.getExecutable().getDeclaringType().getQualifiedName())) {
				printer.write("this");
			} else {
				printer.snapshotLength();
				scan(invocation.getTarget());
				if (printer.hasNewContent()) {
					printer.write(".");
				}
				printer.write("super");
			}
		} else {
			// It's a method invocation
			printer.snapshotLength();
			try (Writable _context = context.modify()) {
				if (invocation.getTarget() instanceof CtTypeAccess) {
					_context.ignoreGenerics(true);
				}
				scan(invocation.getTarget());
			}
			if (printer.hasNewContent()) {
				printer.write(".");
			}

			elementPrinterHelper.writeActualTypeArguments(invocation);
			if (env.isPreserveLineNumbers()) {
				printer.adjustStartPosition(invocation);
			}
			printer.write(invocation.getExecutable().getSimpleName());
		}
		printer.write("(");
		boolean remove = false;
		for (CtExpression<?> e : invocation.getArguments()) {
			scan(e);
			printer.write(", ");
			remove = true;
		}
		if (remove) {
			printer.removeLastChar();
		}
		printer.write(")");
		exitCtExpression(invocation);
	}
	protected boolean addMethodImport(CtExecutableReference ref) {
		if (this.methodImports.containsKey(ref.getSimpleName())) {
			return isImportedInMethodImports(ref);
		}

		// if the whole class is imported: no need to import the method.
		if (declaringTypeIsLocalOrImported(ref.getDeclaringType())) {
			return false;
		}

		methodImports.put(ref.getSimpleName(), ref);

		// if we are in the same package than target type, we also import class to avoid FQN in FQN mode.
		if (ref.getDeclaringType() != null) {
			if (ref.getDeclaringType().getPackage() != null) {
				if (ref.getDeclaringType().getPackage().equals(this.targetType.getPackage())) {
					addClassImport(ref.getDeclaringType());
				}
			}
		}
		return true;
	}
	private boolean declaringTypeIsLocalOrImported(CtTypeReference declaringType) {
		if (declaringType != null) {
			if (!isTypeInCollision(declaringType, false) && addClassImport(declaringType)) {
				return true;
			}

			if (isImportedInClassImports(declaringType) || classNamePresentInJavaLang(declaringType)) {
				return true;
			}

			while (declaringType != null) {
				if (declaringType.equals(targetType)) {
					return true;
				}
				declaringType = declaringType.getDeclaringType();
			}

		}
		return false;
	}
	protected boolean addFieldImport(CtFieldReference ref) {
		if (this.fieldImports.containsKey(ref.getSimpleName())) {
			return isImportedInFieldImports(ref);
		}

		if (declaringTypeIsLocalOrImported(ref.getDeclaringType())) {
			return false;
		}

		fieldImports.put(ref.getSimpleName(), ref);
		return true;
	}
