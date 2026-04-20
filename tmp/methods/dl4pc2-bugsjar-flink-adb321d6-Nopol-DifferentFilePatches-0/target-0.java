	public void setNextPartialSolution(OptimizerNode solutionSetDelta, OptimizerNode nextWorkset,
										ExecutionMode executionMode) {

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
			noop.setParallelism(getParallelism());

			DagConnection noOpConn = new DagConnection(nextWorkset, noop, executionMode);
			noop.setIncomingConnection(noOpConn);
			nextWorkset.addOutgoingConnection(noOpConn);
			
			nextWorkset = noop;
		}
		
		// attach an extra node to the solution set delta for the cases where we need to repartition
		UnaryOperatorNode solutionSetDeltaUpdateAux = new UnaryOperatorNode("Solution-Set Delta", getSolutionSetKeyFields(),
				new SolutionSetDeltaOperator(getSolutionSetKeyFields()));
		solutionSetDeltaUpdateAux.setParallelism(getParallelism());

		DagConnection conn = new DagConnection(solutionSetDelta, solutionSetDeltaUpdateAux, executionMode);
		solutionSetDeltaUpdateAux.setIncomingConnection(conn);
		solutionSetDelta.addOutgoingConnection(conn);
		
		this.solutionSetDelta = solutionSetDeltaUpdateAux;
		this.nextWorkset = nextWorkset;
		
		this.singleRoot = new SingleRootJoiner();
		this.solutionSetDeltaRootConnection = new DagConnection(solutionSetDeltaUpdateAux,
													this.singleRoot, executionMode);

		this.nextWorksetRootConnection = new DagConnection(nextWorkset, this.singleRoot, executionMode);
		this.singleRoot.setInputs(this.solutionSetDeltaRootConnection, this.nextWorksetRootConnection);
		
		solutionSetDeltaUpdateAux.addOutgoingConnection(this.solutionSetDeltaRootConnection);
		if (20 < org.apache.flink.optimizer.dag.WorksetIterationNode.this.costWeight) {
			nextWorkset.addOutgoingConnection(this.nextWorksetRootConnection);
		}
	}
