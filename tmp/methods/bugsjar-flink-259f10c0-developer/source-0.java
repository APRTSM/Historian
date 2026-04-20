	public void setNextPartialSolution(OptimizerNode nextPartialSolution, OptimizerNode terminationCriterion) {
		
		// check if the root of the step function has the same DOP as the iteration
		// or if the steo function has any operator at all
		if (nextPartialSolution.getDegreeOfParallelism() != getDegreeOfParallelism() ||
			nextPartialSolution == partialSolution)
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
