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
	private boolean visit(DumpableNode<?> node, PrintWriter writer, boolean first) {
		// check for duplicate traversal
		if (this.nodeIds.containsKey(node)) {
			return false;
		}
		
		// assign an id first
		this.nodeIds.put(node, this.nodeCnt++);
		
		// then recurse
		for (DumpableNode<?> child : node.getPredecessors()) {
			//This is important, because when the node was already in the graph it is not allowed
			//to set first to false!
			if (visit(child, writer, first)) {
				first = false;
			};
		}
		
		// check if this node should be skipped from the dump
		final OptimizerNode n = node.getOptimizerNode();
		
		// ------------------ dump after the ascend ---------------------
		// start a new node and output node id
		if (!first) {
			writer.print(",\n");	
		}
		// open the node
		writer.print("\t{\n");
		
		// recurse, it is is an iteration node
		if (node instanceof BulkIterationNode || node instanceof BulkIterationPlanNode) {
			
			DumpableNode<?> innerChild = node instanceof BulkIterationNode ?
					((BulkIterationNode) node).getNextPartialSolution() :
					((BulkIterationPlanNode) node).getRootOfStepFunction();
					
			DumpableNode<?> begin = node instanceof BulkIterationNode ?
				((BulkIterationNode) node).getPartialSolution() :
				((BulkIterationPlanNode) node).getPartialSolutionPlanNode();
			
			writer.print("\t\t\"step_function\": [\n");
			
			visit(innerChild, writer, true);
			
			writer.print("\n\t\t],\n");
			writer.print("\t\t\"partial_solution\": " + this.nodeIds.get(begin) + ",\n");
			writer.print("\t\t\"next_partial_solution\": " + this.nodeIds.get(innerChild) + ",\n");
		} else if (node instanceof WorksetIterationNode || node instanceof WorksetIterationPlanNode) {
			
			DumpableNode<?> worksetRoot = node instanceof WorksetIterationNode ?
					((WorksetIterationNode) node).getNextWorkset() :
					((WorksetIterationPlanNode) node).getNextWorkSetPlanNode();
			DumpableNode<?> solutionDelta = node instanceof WorksetIterationNode ?
					((WorksetIterationNode) node).getSolutionSetDelta() :
					((WorksetIterationPlanNode) node).getSolutionSetDeltaPlanNode();
					
			DumpableNode<?> workset = node instanceof WorksetIterationNode ?
						((WorksetIterationNode) node).getWorksetNode() :
						((WorksetIterationPlanNode) node).getWorksetPlanNode();
			DumpableNode<?> solutionSet = node instanceof WorksetIterationNode ?
						((WorksetIterationNode) node).getSolutionSetNode() :
						((WorksetIterationPlanNode) node).getSolutionSetPlanNode();
			
			writer.print("\t\t\"step_function\": [\n");
			
			visit(worksetRoot, writer, true);
			visit(solutionDelta, writer, false);
			
			writer.print("\n\t\t],\n");
			writer.print("\t\t\"workset\": " + this.nodeIds.get(workset) + ",\n");
			writer.print("\t\t\"solution_set\": " + this.nodeIds.get(solutionSet) + ",\n");
			writer.print("\t\t\"next_workset\": " + this.nodeIds.get(worksetRoot) + ",\n");
			writer.print("\t\t\"solution_delta\": " + this.nodeIds.get(solutionDelta) + ",\n");
		}
		
		// print the id
		writer.print("\t\t\"id\": " + this.nodeIds.get(node));

		
		final String type;
		final String contents;
		if (n instanceof DataSinkNode) {
			type = "sink";
			contents = n.getPactContract().toString();
		} else if (n instanceof DataSourceNode) {
			type = "source";
			contents = n.getPactContract().toString();
		} else if (n instanceof BulkIterationNode) {
			type = "bulk_iteration";
			contents = n.getPactContract().getName();
		} else if (n instanceof WorksetIterationNode) {
			type = "workset_iteration";
			contents = n.getPactContract().getName();
		} else if (n instanceof BinaryUnionNode) {
			type = "pact";
			contents = "";
		} else {
			type = "pact";
			contents = n.getPactContract().getName();
		}
		
		String name = n.getName();
		if (name.equals("Reduce") && (node instanceof SingleInputPlanNode) && 
				((SingleInputPlanNode) node).getDriverStrategy() == DriverStrategy.SORTED_GROUP_COMBINE) {
			name = "Combine";
		}
		
		// output the type identifier
		writer.print(",\n\t\t\"type\": \"" + type + "\"");
		
		// output node name
		writer.print(",\n\t\t\"pact\": \"" + name + "\"");
		
		// output node contents
		writer.print(",\n\t\t\"contents\": \"" + contents + "\"");

		// degree of parallelism
		writer.print(",\n\t\t\"parallelism\": \""
			+ (n.getDegreeOfParallelism() >= 1 ? n.getDegreeOfParallelism() : "default") + "\"");
		
		// output node predecessors
		Iterator<? extends DumpableConnection<?>> inConns = node.getDumpableInputs().iterator();
		String child1name = "", child2name = "";

		if (inConns != null && inConns.hasNext()) {
			// start predecessor list
			writer.print(",\n\t\t\"predecessors\": [");
			int inputNum = 0;
			
			while (inConns.hasNext()) {
				final DumpableConnection<?> inConn = inConns.next();
				final DumpableNode<?> source = inConn.getSource();
				writer.print(inputNum == 0 ? "\n" : ",\n");
				if (inputNum == 0) {
					child1name += child1name.length() > 0 ? ", " : "";
					child1name += source.getOptimizerNode().getPactContract().getName();
				} else if (inputNum == 1) {
					child2name += child2name.length() > 0 ? ", " : "";
					child2name = source.getOptimizerNode().getPactContract().getName();
				}

				// output predecessor id
				writer.print("\t\t\t{\"id\": " + this.nodeIds.get(source));

				// output connection side
				if (inConns.hasNext() || inputNum > 0) {
					writer.print(", \"side\": \"" + (inputNum == 0 ? "first" : "second") + "\"");
				}
				// output shipping strategy and channel type
				final Channel channel = (inConn instanceof Channel) ? (Channel) inConn : null;
				final ShipStrategyType shipType = channel != null ? channel.getShipStrategy() :
						((PactConnection) inConn).getShipStrategy();
					
				String shipStrategy = null;
				if (shipType != null) {
					switch (shipType) {
					case NONE:
						// nothing
						break;
					case FORWARD:
						shipStrategy = "Forward";
						break;
					case BROADCAST:
						shipStrategy = "Broadcast";
						break;
					case PARTITION_HASH:
						shipStrategy = "Hash Partition";
						break;
					case PARTITION_RANGE:
						shipStrategy = "Range Partition";
						break;
					case PARTITION_RANDOM:
						shipStrategy = "Redistribute";
						break;
					case PARTITION_FORCED_REBALANCE:
						shipStrategy = "Rebalance";
						break;
					default:
						throw new CompilerException("Unknown ship strategy '" + inConn.getShipStrategy().name()
							+ "' in JSON generator.");
					}
				}

				if (channel != null && channel.getShipStrategyKeys() != null && channel.getShipStrategyKeys().size() > 0) {
					shipStrategy += " on " + (channel.getShipStrategySortOrder() == null ?
							channel.getShipStrategyKeys().toString() :
							Utils.createOrdering(channel.getShipStrategyKeys(), channel.getShipStrategySortOrder()).toString());
				}

				if (shipStrategy != null) {
					writer.print(", \"ship_strategy\": \"" + shipStrategy + "\"");
				}
				
				if (channel != null) {
					String localStrategy = null;
					switch (channel.getLocalStrategy()) {
					case NONE:
						break;
					case SORT:
						localStrategy = "Sort";
						break;
					case COMBININGSORT:
						localStrategy = "Sort (combining)";
						break;
					default:
						throw new CompilerException("Unknown local strategy " + channel.getLocalStrategy().name());
					}
					
					if (channel != null && channel.getLocalStrategyKeys() != null && channel.getLocalStrategyKeys().size() > 0) {
						localStrategy += " on " + (channel.getLocalStrategySortOrder() == null ?
								channel.getLocalStrategyKeys().toString() :
								Utils.createOrdering(channel.getLocalStrategyKeys(), channel.getLocalStrategySortOrder()).toString());
					}
					
					if (localStrategy != null) {
						writer.print(", \"local_strategy\": \"" + localStrategy + "\"");
					}
					
					if (channel != null && channel.getTempMode() != TempMode.NONE) {
						String tempMode = channel.getTempMode().toString();
						writer.print(", \"temp_mode\": \"" + tempMode + "\"");
					}
				}

				writer.print('}');
				inputNum++;
			}
			// finish predecessors
			writer.print("\n\t\t]");
		}
