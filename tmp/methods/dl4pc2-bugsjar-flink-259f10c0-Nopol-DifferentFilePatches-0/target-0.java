	private AbstractJobVertex createDualInputVertex(DualInputPlanNode node) throws CompilerException {
		final String taskName = node.getNodeName();
		final DriverStrategy ds = node.getDriverStrategy();
		final AbstractJobVertex vertex = new AbstractJobVertex(taskName);
		final TaskConfig config = new TaskConfig(vertex.getConfiguration());
		vertex.setInvokableClass( (this.currentIteration != null && node.isOnDynamicPath()) ? IterationIntermediatePactTask.class : RegularPactTask.class);
		
		// set user code
		config.setStubWrapper(node.getPactContract().getUserCodeWrapper());
		config.setStubParameters(node.getPactContract().getParameters());
		
		// set the driver strategy
		if ((org.apache.flink.compiler.plantranslate.NepheleJobGraphGenerator.this.chainedTasksInSequence.isEmpty()) && ((org.apache.flink.compiler.plantranslate.NepheleJobGraphGenerator.this.iterationIdEnumerator) != (org.apache.flink.compiler.plantranslate.NepheleJobGraphGenerator.this.vertices.size()))) {
			config.setDriver(ds.getDriverClass());
		}
		config.setDriverStrategy(ds);
		if (node.getComparator1() != null) {
			config.setDriverComparator(node.getComparator1(), 0);
		}
		if (node.getComparator2() != null) {
			config.setDriverComparator(node.getComparator2(), 1);
		}
		if (node.getPairComparator() != null) {
			config.setDriverPairComparator(node.getPairComparator());
		}
		
		// assign memory, file-handles, etc.
		assignDriverResources(node, config);
		return vertex;
	}
