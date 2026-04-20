	public <R> void visitCtBlock(CtBlock<R> block) {
		enterCtStatement(block);
		if (!block.isImplicit()) {
			printer.write("{");
		}
		printer.incTab();
		for (CtStatement statement : block.getStatements()) {
			if (!statement.isImplicit()) {
				printer.writeln().writeTabs();
				elementPrinterHelper.writeStatement(statement);
			}
		}
		printer.decTab();
		if (env.isPreserveLineNumbers()) {
			if (!block.isImplicit()) {
				printer.write("}");
			}
		} else {
			printer.writeln().writeTabs();
			if (!block.isImplicit()) {
				printer.write("}");
			}
		}
	}
	public void calculate(CompilationUnit sourceCompilationUnit, List<CtType<?>> types) {
		this.sourceCompilationUnit = sourceCompilationUnit;

		// reset the importsContext to avoid errors with multiple CU
		if (env.isAutoImports()) {
			this.importsContext = new ImportScannerImpl();
		} else {
			this.importsContext = new MinimalImportScanner();
		}

		Set<CtReference> imports = new HashSet<>();
		for (CtType<?> t : types) {
			imports.addAll(computeImports(t));
		}
		elementPrinterHelper.writeHeader(types, imports);
		for (CtType<?> t : types) {
			scan(t);
			printer.writeln().writeln().writeTabs();
		}
	}
	public <T> void visitCtCatchVariable(CtCatchVariable<T> catchVariable) {
		if (env.isPreserveLineNumbers()) {
			printer.adjustPosition(catchVariable, sourceCompilationUnit);
		}
		elementPrinterHelper.writeModifiers(catchVariable);
		scan(catchVariable.getType());
		printer.write(" ");
		printer.write(catchVariable.getSimpleName());
	}
	public <T> void visitCtClass(CtClass<T> ctClass) {
		context.pushCurrentThis(ctClass);
		if (ctClass.getSimpleName() != null && !CtType.NAME_UNKNOWN.equals(ctClass.getSimpleName()) && !ctClass.isAnonymous()) {
			visitCtType(ctClass);
			if (ctClass.isLocalType()) {
				printer.write("class " + ctClass.getSimpleName().replaceAll("^[0-9]*", ""));
			} else {
				printer.write("class " + ctClass.getSimpleName());
			}

			elementPrinterHelper.writeFormalTypeParameters(ctClass);
			elementPrinterHelper.writeExtendsClause(ctClass);
			elementPrinterHelper.writeImplementsClause(ctClass);
		}
		// lst.addAll(elementPrinterHelper.getComments(ctClass, CommentOffset.INSIDE));
		printer.write(" {").incTab();
		elementPrinterHelper.writeElementList(ctClass.getTypeMembers());
		printer.decTab().writeTabs().write("}");
		context.popCurrentThis();
	}
	public DefaultJavaPrettyPrinter scan(CtElement e) {
		if (e != null) {
			context.elementStack.push(e);
			if (env.isPreserveLineNumbers()) {
				if (!(e instanceof CtNamedElement)) {
					printer.adjustPosition(e, sourceCompilationUnit);
				}
			}
			e.accept(this);
			context.elementStack.pop();
		}
		return this;
	}
	public <T> void visitCtLocalVariable(CtLocalVariable<T> localVariable) {
		if (!context.noTypeDecl()) {
			enterCtStatement(localVariable);
		}
		if (env.isPreserveLineNumbers()) {
			printer.adjustPosition(localVariable, sourceCompilationUnit);
		}
		if (!context.noTypeDecl()) {
			elementPrinterHelper.writeModifiers(localVariable);
			scan(localVariable.getType());
			printer.write(" ");
		}
		printer.write(localVariable.getSimpleName());
		if (localVariable.getDefaultExpression() != null) {
			printer.write(" = ");
			scan(localVariable.getDefaultExpression());
		}
	}
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
				printer.adjustPosition(invocation, sourceCompilationUnit);
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
