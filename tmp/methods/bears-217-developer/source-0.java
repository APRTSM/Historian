	public boolean visit(CompilationUnitDeclaration compilationUnitDeclaration, CompilationUnitScope scope) {
		context.compilationunitdeclaration = scope.referenceContext;
		context.compilationUnitSpoon = getFactory().CompilationUnit().getOrCreate(new String(context.compilationunitdeclaration.getFileName()));
		ModuleBinding enclosingModule = scope.fPackage.enclosingModule;

		CtModule module;
		if (!enclosingModule.isUnnamed() && enclosingModule.shortReadableName() != null && enclosingModule.shortReadableName().length > 0) {
			module = getFactory().Module().getOrCreate(String.valueOf(enclosingModule.shortReadableName()));
		} else {
			module = getFactory().Module().getUnnamedModule();
		}

		context.compilationUnitSpoon.setDeclaredPackage(getFactory().Package().getOrCreate(CharOperation.toString(scope.currentPackageName), module));
		CtPackageDeclaration packageDeclaration = context.compilationUnitSpoon.getPackageDeclaration();
		if (packageDeclaration != null) {
			ImportReference packageRef = compilationUnitDeclaration.currentPackage;
			packageDeclaration.setPosition(factory.Core().createCompoundSourcePosition(
					context.compilationUnitSpoon, packageRef.sourceStart(), packageRef.sourceEnd(), packageRef.declarationSourceStart, packageRef.declarationEnd, context.compilationUnitSpoon.getLineSeparatorPositions()));
		}
		return true;
	}
