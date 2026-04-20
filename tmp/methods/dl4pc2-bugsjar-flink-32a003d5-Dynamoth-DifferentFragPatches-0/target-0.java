	private OptimizedPlan compile(Plan program, OptimizerPostPass postPasser) throws CompilerException {
		if (program == null || postPasser == null) {
			throw new NullPointerException();
		}
		
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("Beginning compilation of program '" + program.getJobName() + '\'');
		}

		// set the default degree of parallelism
		int defaultParallelism = program.getDefaultParallelism() > 0 ?
			program.getDefaultParallelism() : this.defaultDegreeOfParallelism;

		// log the output
		if (LOG.isDebugEnabled()) {
			LOG.debug("Using a default degree of parallelism of " + defaultParallelism + '.');
		}

		// the first step in the compilation is to create the optimizer plan representation
		// this step does the following:
		// 1) It creates an optimizer plan node for each operator
		// 2) It connects them via channels
		// 3) It looks for hints about local strategies and channel types and
		// sets the types and strategies accordingly
		// 4) It makes estimates about the data volume of the data sources and
		// propagates those estimates through the plan

		GraphCreatingVisitor graphCreator = new GraphCreatingVisitor(defaultParallelism);
		program.accept(graphCreator);

		// if we have a plan with multiple data sinks, add logical optimizer nodes that have two data-sinks as children
		// each until we have only a single root node. This allows to transparently deal with the nodes with
		// multiple outputs
		OptimizerNode rootNode;
		if (graphCreator.sinks.size() == 1) {
			rootNode = graphCreator.sinks.get(0);
		} else if (graphCreator.sinks.size() > 1) {
			Iterator<DataSinkNode> iter = graphCreator.sinks.iterator();
			rootNode = iter.next();

			while (iter.hasNext()) {
				rootNode = new SinkJoiner(rootNode, iter.next());
			}
		} else {
			throw new CompilerException("Bug: The optimizer plan representation has no sinks.");
		}

		// now that we have all nodes created and recorded which ones consume memory, tell the nodes their minimal
		// guaranteed memory, for further cost estimations. we assume an equal distribution of memory among consumer tasks
		
		rootNode.accept(new IdAndEstimatesVisitor(this.statistics));
		
		// Now that the previous step is done, the next step is to traverse the graph again for the two
		// steps that cannot directly be performed during the plan enumeration, because we are dealing with DAGs
		// rather than a trees. That requires us to deviate at some points from the classical DB optimizer algorithms.
		//
		// 1) propagate the interesting properties top-down through the graph
		// 2) Track information about nodes with multiple outputs that are later on reconnected in a node with
		// multiple inputs.
		InterestingPropertyVisitor propsVisitor = new InterestingPropertyVisitor(this.costEstimator);
		rootNode.accept(propsVisitor);
		
		BranchesVisitor branchingVisitor = new BranchesVisitor();
		rootNode.accept(branchingVisitor);
		
		// perform a sanity check: the root may not have any unclosed branches
		if (rootNode.getOpenBranches() != null && rootNode.getOpenBranches().size() > 0) {
			throw new CompilerException("Bug: Logic for branching plans (non-tree plans) has an error, and does not " +
					"track the re-joining of branches correctly.");
		}

		// the final step is now to generate the actual plan alternatives
		List<PlanNode> bestPlan = rootNode.getAlternativePlans(this.costEstimator);

		if (bestPlan.size() != 1) {
			throw new CompilerException("Error in compiler: more than one best plan was created!");
		}

		// check if the best plan's root is a data sink (single sink plan)
		// if so, directly take it. if it is a sink joiner node, get its contained sinks
		PlanNode bestPlanRoot = bestPlan.get(0);
		List<SinkPlanNode> bestPlanSinks = new ArrayList<SinkPlanNode>(4);

		if (bestPlanRoot instanceof SinkPlanNode) {
			bestPlanSinks.add((SinkPlanNode) bestPlanRoot);
		} else if (bestPlanRoot instanceof SinkJoinerPlanNode) {
			if (defaultParallelism != program.getDefaultParallelism()) {
				((SinkJoinerPlanNode) bestPlanRoot).getDataSinks(bestPlanSinks);
			}
		}
		
		DeadlockPreventer dp = new DeadlockPreventer();
		dp.resolveDeadlocks(bestPlanSinks);

		// finalize the plan
		OptimizedPlan plan = new PlanFinalizer().createFinalPlan(bestPlanSinks, program.getJobName(), program);

		// swap the binary unions for n-ary unions. this changes no strategies or memory consumers whatsoever, so
		// we can do this after the plan finalization
		plan.accept(new BinaryUnionReplacer());
		
		// post pass the plan. this is the phase where the serialization and comparator code is set
		postPasser.postPass(plan);
		
		return plan;
	}
