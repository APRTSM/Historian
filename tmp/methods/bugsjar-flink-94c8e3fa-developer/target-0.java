		public void postVisit(Operator<?> c) {
			
			OptimizerNode n = this.con2node.get(c);

			// first connect to the predecessors
			n.setInput(this.con2node);
			n.setBroadcastInputs(this.con2node);
			
			// if the node represents a bulk iteration, we recursively translate the data flow now
			if (n instanceof BulkIterationNode) {
				final BulkIterationNode iterNode = (BulkIterationNode) n;
				final BulkIterationBase<?> iter = iterNode.getIterationContract();

				// pass a copy of the no iterative part into the iteration translation,
				// in case the iteration references its closure
				HashMap<Operator<?>, OptimizerNode> closure = new HashMap<Operator<?>, OptimizerNode>(con2node);

				// first, recursively build the data flow for the step function
				final GraphCreatingVisitor recursiveCreator = new GraphCreatingVisitor(this, true,
					iterNode.getDegreeOfParallelism(), closure);
				
				BulkPartialSolutionNode partialSolution = null;
				
				iter.getNextPartialSolution().accept(recursiveCreator);
				
				partialSolution =  (BulkPartialSolutionNode) recursiveCreator.con2node.get(iter.getPartialSolution());
				OptimizerNode rootOfStepFunction = recursiveCreator.con2node.get(iter.getNextPartialSolution());
				if (partialSolution == null) {
					throw new CompilerException("Error: The step functions result does not depend on the partial solution.");
				}
				
				
				OptimizerNode terminationCriterion = null;
				
				if (iter.getTerminationCriterion() != null) {
					terminationCriterion = recursiveCreator.con2node.get(iter.getTerminationCriterion());
					
					// no intermediate node yet, traverse from the termination criterion to build the missing parts
					if (terminationCriterion == null) {
						iter.getTerminationCriterion().accept(recursiveCreator);
						terminationCriterion = recursiveCreator.con2node.get(iter.getTerminationCriterion());
					}
				}
				
				iterNode.setPartialSolution(partialSolution);
				iterNode.setNextPartialSolution(rootOfStepFunction, terminationCriterion);
				
				// go over the contained data flow and mark the dynamic path nodes
				StaticDynamicPathIdentifier identifier = new StaticDynamicPathIdentifier(iterNode.getCostWeight());
				iterNode.acceptForStepFunction(identifier);
			}
			else if (n instanceof WorksetIterationNode) {
				final WorksetIterationNode iterNode = (WorksetIterationNode) n;
				final DeltaIterationBase<?, ?> iter = iterNode.getIterationContract();

				// we need to ensure that both the next-workset and the solution-set-delta depend on the workset. One check is for free
				// during the translation, we do the other check here as a pre-condition
				{
					StepFunctionValidator wsf = new StepFunctionValidator();
					iter.getNextWorkset().accept(wsf);
					if (!wsf.foundWorkset) {
						throw new CompilerException("In the given program, the next workset does not depend on the workset. This is a prerequisite in delta iterations.");
					}
				}
				
				// calculate the closure of the anonymous function
				HashMap<Operator<?>, OptimizerNode> closure = new HashMap<Operator<?>, OptimizerNode>(con2node);

				// first, recursively build the data flow for the step function
				final GraphCreatingVisitor recursiveCreator = new GraphCreatingVisitor(this, true, iterNode.getDegreeOfParallelism(), closure);
				
				// descend from the solution set delta. check that it depends on both the workset
				// and the solution set. If it does depend on both, this descend should create both nodes
				iter.getSolutionSetDelta().accept(recursiveCreator);
				
				final WorksetNode worksetNode = (WorksetNode) recursiveCreator.con2node.get(iter.getWorkset());
				
				if (worksetNode == null) {
					throw new CompilerException("In the given program, the solution set delta does not depend on the workset. This is a prerequisite in delta iterations.");
				}
				
				iter.getNextWorkset().accept(recursiveCreator);
				
				SolutionSetNode solutionSetNode = (SolutionSetNode) recursiveCreator.con2node.get(iter.getSolutionSet());
				
				if (solutionSetNode == null || solutionSetNode.getOutgoingConnections() == null || solutionSetNode.getOutgoingConnections().isEmpty()) {
					solutionSetNode = new SolutionSetNode((SolutionSetPlaceHolder<?>) iter.getSolutionSet(), iterNode);
				}
				else {
					for (PactConnection conn : solutionSetNode.getOutgoingConnections()) {
						OptimizerNode successor = conn.getTarget();
					
						if (successor.getClass() == JoinNode.class) {
							// find out which input to the match the solution set is
							JoinNode mn = (JoinNode) successor;
							if (mn.getFirstPredecessorNode() == solutionSetNode) {
								mn.makeJoinWithSolutionSet(0);
							} else if (mn.getSecondPredecessorNode() == solutionSetNode) {
								mn.makeJoinWithSolutionSet(1);
							} else {
								throw new CompilerException();
							}
						}
						else if (successor.getClass() == CoGroupNode.class) {
							CoGroupNode cg = (CoGroupNode) successor;
							if (cg.getFirstPredecessorNode() == solutionSetNode) {
								cg.makeCoGroupWithSolutionSet(0);
							} else if (cg.getSecondPredecessorNode() == solutionSetNode) {
								cg.makeCoGroupWithSolutionSet(1);
							} else {
								throw new CompilerException();
							}
						}
						else {
							throw new InvalidProgramException("Error: The only operations allowed on the solution set are Join and CoGroup.");
						}
					}
				}
				
				final OptimizerNode nextWorksetNode = recursiveCreator.con2node.get(iter.getNextWorkset());
				final OptimizerNode solutionSetDeltaNode = recursiveCreator.con2node.get(iter.getSolutionSetDelta());
				
				// set the step function nodes to the iteration node
				iterNode.setPartialSolution(solutionSetNode, worksetNode);
				iterNode.setNextPartialSolution(solutionSetDeltaNode, nextWorksetNode);
				
				// go over the contained data flow and mark the dynamic path nodes
				StaticDynamicPathIdentifier pathIdentifier = new StaticDynamicPathIdentifier(iterNode.getCostWeight());
				iterNode.acceptForStepFunction(pathIdentifier);
			}
		}
