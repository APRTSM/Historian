	protected void buildModel(CompilationUnitDeclaration[] units) {
		JDTTreeBuilder builder = new JDTTreeBuilder(factory);
		unitLoop:
		for (CompilationUnitDeclaration unit : units) {
			if (!unit.isEmpty()) {
				final String unitPath = new String(unit.getFileName());
				for (final CompilationUnitFilter cuf : compilationUnitFilters) {
					if (cuf.exclude(unitPath)) {
						// do not traverse this unit
						continue unitLoop;
					}
				}
				unit.traverse(builder, unit.scope);
				if (getFactory().getEnvironment().isCommentsEnabled()) {
					new JDTCommentBuilder(unit, factory).build();
				}
			}
		}
	}
