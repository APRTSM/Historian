	public void calculate(CompilationUnit sourceCompilationUnit, List<CtType<?>> types) {
		this.sourceCompilationUnit = sourceCompilationUnit;
		// reset the import scanner between each compilationunit
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
	public <T> void visitCtFieldRead(CtFieldRead<T> fieldRead) {
		enter(fieldRead);
		scan(fieldRead.getVariable());
		scan(fieldRead.getAnnotations());
		scan(fieldRead.getTypeCasts());
		scan(fieldRead.getVariable());
		scan(fieldRead.getTarget());
		exit(fieldRead);
	}
	public Collection<CtReference> computeAllImports(CtType<?> simpleType) {
		classImports.clear();
		fieldImports.clear();
		methodImports.clear();
		//look for top declaring type of that simpleType
		targetType = simpleType.getReference().getTopLevelType();
		addClassImport(simpleType.getReference());
		scan(simpleType);

		Collection<CtReference> listallImports = new ArrayList<>();
		listallImports.addAll(this.classImports.values());
		listallImports.addAll(this.fieldImports.values());
		listallImports.addAll(this.methodImports.values());
		return listallImports;
	}
	public <T> void visitCtInvocation(CtInvocation<T> invocation) {
		enter(invocation);
		scan(invocation.getAnnotations());
		scan(invocation.getTypeCasts());
		scan(invocation.getTarget());
		scan(invocation.getExecutable());
		scan(invocation.getArguments());
		exit(invocation);
	}
	private boolean declaringTypeIsLocalOrImported(CtTypeReference declaringType) {
		if (declaringType != null) {
			if (isImportedInClassImports(declaringType) || classNamePresentInJavaLang(declaringType)) {
				return true;
			}

			if (!isTypeInCollision(declaringType, false) && addClassImport(declaringType)) {
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
	public <T> void visitCtFieldWrite(CtFieldWrite<T> fieldWrite) {
		enter(fieldWrite);
		scan(fieldWrite.getVariable());
		scan(fieldWrite.getAnnotations());
		scan(fieldWrite.getTypeCasts());
		scan(fieldWrite.getVariable());
		scan(fieldWrite.getTarget());
		exit(fieldWrite);
	}
	public Collection<CtTypeReference<?>> computeImports(CtElement element) {
		classImports.clear();
		fieldImports.clear();
		methodImports.clear();
		//look for top declaring type of that simpleType

		if (element instanceof CtType) {
			CtType simpleType = (CtType) element;
			targetType = simpleType.getReference().getTopLevelType();
			addClassImport(simpleType.getReference());
			scan(simpleType);
		} else {
			CtType<?> type = element.getParent(CtType.class);
			targetType = type == null ? null : type.getReference().getTopLevelType();
			scan(element);
		}
		return this.classImports.values();
	}
