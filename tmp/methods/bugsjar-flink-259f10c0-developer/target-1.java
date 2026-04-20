	public void setNextPartialSolution(OptimizerNode nextPartialSolution, OptimizerNode terminationCriterion) {
		
		// check if the root of the step function has the same DOP as the iteration
		// or if the steo function has any operator at all
		if (nextPartialSolution.getDegreeOfParallelism() != getDegreeOfParallelism() ||
			nextPartialSolution == partialSolution || nextPartialSolution instanceof BinaryUnionNode)
		{
			// add a no-op to the root to express the re-partitioning
			NoOpNode noop = new NoOpNode();
			noop.setDegreeOfParallelism(getDegreeOfParallelism());

			PactConnection noOpConn = new PactConnection(nextPartialSolution, noop);
			noop.setIncomingConnection(noOpConn);
			nextPartialSolution.addOutgoingConnection(noOpConn);
			
			nextPartialSolution = noop;
		}
		
		this.nextPartialSolution = nextPartialSolution;
		this.terminationCriterion = terminationCriterion;
		
		if (terminationCriterion == null) {
			this.singleRoot = nextPartialSolution;
			this.rootConnection = new PactConnection(nextPartialSolution);
		}
		else {
			// we have a termination criterion
			SingleRootJoiner singleRootJoiner = new SingleRootJoiner();
			this.rootConnection = new PactConnection(nextPartialSolution, singleRootJoiner);
			this.terminationCriterionRootConnection = new PactConnection(terminationCriterion, singleRootJoiner);
			singleRootJoiner.setInputs(this.rootConnection, this.terminationCriterionRootConnection);
			
			this.singleRoot = singleRootJoiner;
			
			// add connection to terminationCriterion for interesting properties visitor
			terminationCriterion.addOutgoingConnection(terminationCriterionRootConnection);
		
		}
		
		nextPartialSolution.addOutgoingConnection(rootConnection);
	}
	public void setNextPartialSolution(OptimizerNode solutionSetDelta, OptimizerNode nextWorkset) {
		// check whether the next partial solution is itself the join with
		// the partial solution (so we can potentially do direct updates)
		if (solutionSetDelta instanceof TwoInputNode) {
			TwoInputNode solutionDeltaTwoInput = (TwoInputNode) solutionSetDelta;
			if (solutionDeltaTwoInput.getFirstPredecessorNode() == this.solutionSetNode ||
				solutionDeltaTwoInput.getSecondPredecessorNode() == this.solutionSetNode)
			{
				this.solutionDeltaImmediatelyAfterSolutionJoin = true;
			}
		}
		
		// there needs to be at least one node in the workset path, so
		// if the next workset is equal to the workset, we need to inject a no-op node
		if (nextWorkset == worksetNode || nextWorkset instanceof BinaryUnionNode) {
			NoOpNode noop = new NoOpNode();
			noop.setDegreeOfParallelism(getDegreeOfParallelism());

			PactConnection noOpConn = new PactConnection(nextWorkset, noop);
			noop.setIncomingConnection(noOpConn);
			nextWorkset.addOutgoingConnection(noOpConn);
			
			nextWorkset = noop;
		}
		
		// attach an extra node to the solution set delta for the cases where we need to repartition
		UnaryOperatorNode solutionSetDeltaUpdateAux = new UnaryOperatorNode("Solution-Set Delta", getSolutionSetKeyFields(),
				new SolutionSetDeltaOperator(getSolutionSetKeyFields()));
		solutionSetDeltaUpdateAux.setDegreeOfParallelism(getDegreeOfParallelism());

		PactConnection conn = new PactConnection(solutionSetDelta, solutionSetDeltaUpdateAux);
		solutionSetDeltaUpdateAux.setIncomingConnection(conn);
		solutionSetDelta.addOutgoingConnection(conn);
		
		this.solutionSetDelta = solutionSetDeltaUpdateAux;
		this.nextWorkset = nextWorkset;
		
		this.singleRoot = new SingleRootJoiner();
		this.solutionSetDeltaRootConnection = new PactConnection(solutionSetDeltaUpdateAux, this.singleRoot);
		this.nextWorksetRootConnection = new PactConnection(nextWorkset, this.singleRoot);
		this.singleRoot.setInputs(this.solutionSetDeltaRootConnection, this.nextWorksetRootConnection);
		
		solutionSetDeltaUpdateAux.addOutgoingConnection(this.solutionSetDeltaRootConnection);
		nextWorkset.addOutgoingConnection(this.nextWorksetRootConnection);
	}
