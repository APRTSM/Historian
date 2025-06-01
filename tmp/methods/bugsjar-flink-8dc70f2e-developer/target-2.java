	protected void instantiateCandidate(OperatorDescriptorSingle dps, Channel in, List<Set<? extends NamedChannel>> broadcastPlanChannels, 
			List<PlanNode> target, CostEstimator estimator, RequestedGlobalProperties globPropsReq, RequestedLocalProperties locPropsReq)
	{
		// NOTES ON THE ENUMERATION OF THE STEP FUNCTION PLANS:
		// Whenever we instantiate the iteration, we enumerate new candidates for the step function.
		// That way, we make sure we have an appropriate plan for each candidate for the initial partial solution,
		// we have a fitting candidate for the step function (often, work is pushed out of the step function).
		// Among the candidates of the step function, we keep only those that meet the requested properties of the
		// current candidate initial partial solution. That makes sure these properties exist at the beginning of
		// the successive iteration.
		
		// 1) Because we enumerate multiple times, we may need to clean the cached plans
		//    before starting another enumeration
		this.nextPartialSolution.accept(PlanCacheCleaner.INSTANCE);
		if (this.terminationCriterion != null) {
			this.terminationCriterion.accept(PlanCacheCleaner.INSTANCE);
		}
		
		// 2) Give the partial solution the properties of the current candidate for the initial partial solution
		this.partialSolution.setCandidateProperties(in.getGlobalProperties(), in.getLocalProperties(), in);
		final BulkPartialSolutionPlanNode pspn = this.partialSolution.getCurrentPartialSolutionPlanNode();
		
		// 3) Get the alternative plans
		List<PlanNode> candidates = this.nextPartialSolution.getAlternativePlans(estimator);
		
		// 4) Make sure that the beginning of the step function does not assume properties that 
		//    are not also produced by the end of the step function.

		{
			List<PlanNode> newCandidates = new ArrayList<PlanNode>();
			
			for (Iterator<PlanNode> planDeleter = candidates.iterator(); planDeleter.hasNext(); ) {
				PlanNode candidate = planDeleter.next();
				
				GlobalProperties atEndGlobal = candidate.getGlobalProperties();
				LocalProperties atEndLocal = candidate.getLocalProperties();
				
				FeedbackPropertiesMeetRequirementsReport report = candidate.checkPartialSolutionPropertiesMet(pspn, atEndGlobal, atEndLocal);
				if (report == FeedbackPropertiesMeetRequirementsReport.NO_PARTIAL_SOLUTION) {
					; // depends only through broadcast variable on the partial solution
				}
				else if (report == FeedbackPropertiesMeetRequirementsReport.NOT_MET) {
					// attach a no-op node through which we create the properties of the original input
					Channel toNoOp = new Channel(candidate);
					globPropsReq.parameterizeChannel(toNoOp, false, rootConnection.getDataExchangeMode(), false);
					locPropsReq.parameterizeChannel(toNoOp);

					NoOpUnaryUdfOp noOpUnaryUdfOp = new NoOpUnaryUdfOp<>();
					noOpUnaryUdfOp.setInput(candidate.getProgramOperator());
					UnaryOperatorNode rebuildPropertiesNode = new UnaryOperatorNode("Rebuild Partial Solution Properties", noOpUnaryUdfOp, true);
					rebuildPropertiesNode.setParallelism(candidate.getParallelism());
					
					SingleInputPlanNode rebuildPropertiesPlanNode = new SingleInputPlanNode(rebuildPropertiesNode, "Rebuild Partial Solution Properties", toNoOp, DriverStrategy.UNARY_NO_OP);
					rebuildPropertiesPlanNode.initProperties(toNoOp.getGlobalProperties(), toNoOp.getLocalProperties());
					estimator.costOperator(rebuildPropertiesPlanNode);
						
					GlobalProperties atEndGlobalModified = rebuildPropertiesPlanNode.getGlobalProperties();
					LocalProperties atEndLocalModified = rebuildPropertiesPlanNode.getLocalProperties();
						
					if (!(atEndGlobalModified.equals(atEndGlobal) && atEndLocalModified.equals(atEndLocal))) {
						FeedbackPropertiesMeetRequirementsReport report2 = candidate.checkPartialSolutionPropertiesMet(pspn, atEndGlobalModified, atEndLocalModified);
						
						if (report2 != FeedbackPropertiesMeetRequirementsReport.NOT_MET) {
							newCandidates.add(rebuildPropertiesPlanNode);
						}
					}
					
					planDeleter.remove();
				}
			}

			candidates.addAll(newCandidates);
		}

		if (candidates.isEmpty()) {
			return;
		}
		
		// 5) Create a candidate for the Iteration Node for every remaining plan of the step function.
		if (terminationCriterion == null) {
			for (PlanNode candidate : candidates) {
				BulkIterationPlanNode node = new BulkIterationPlanNode(this, this.getOperator().getName(), in, pspn, candidate);
				GlobalProperties gProps = candidate.getGlobalProperties().clone();
				LocalProperties lProps = candidate.getLocalProperties().clone();
				node.initProperties(gProps, lProps);
				target.add(node);
			}
		}
		else if (candidates.size() > 0) {
			List<PlanNode> terminationCriterionCandidates = this.terminationCriterion.getAlternativePlans(estimator);

			SingleRootJoiner singleRoot = (SingleRootJoiner) this.singleRoot;
			
			for (PlanNode candidate : candidates) {
				for (PlanNode terminationCandidate : terminationCriterionCandidates) {
					if (singleRoot.areBranchCompatible(candidate, terminationCandidate)) {
						BulkIterationPlanNode node = new BulkIterationPlanNode(this, "BulkIteration ("+this.getOperator().getName()+")", in, pspn, candidate, terminationCandidate);
						GlobalProperties gProps = candidate.getGlobalProperties().clone();
						LocalProperties lProps = candidate.getLocalProperties().clone();
						node.initProperties(gProps, lProps);
						target.add(node);
						
					}
				}
			}
			
		}
	}
	public UnaryOperatorNode(String name, FieldSet keys, List<OperatorDescriptorSingle> operators) {
		super(keys);
		
		this.operators = operators;
		this.name = name;
	}
	public UnaryOperatorNode(String name, SingleInputOperator<?, ?, ?> operator, boolean onDynamicPath) {
		super(operator);

		this.name = name;
		this.operators = new ArrayList<>();
		this.onDynamicPath = onDynamicPath;
	}
	protected List<OperatorDescriptorSingle> getPossibleProperties() {
		return this.operators;
	}
	protected void instantiate(OperatorDescriptorDual operator, Channel solutionSetIn, Channel worksetIn,
			List<Set<? extends NamedChannel>> broadcastPlanChannels, List<PlanNode> target, CostEstimator estimator,
			RequestedGlobalProperties globPropsReqSolutionSet, RequestedGlobalProperties globPropsReqWorkset,
			RequestedLocalProperties locPropsReqSolutionSet, RequestedLocalProperties locPropsReqWorkset)
	{
		// check for pipeline breaking using hash join with build on the solution set side
		placePipelineBreakersIfNecessary(DriverStrategy.HYBRIDHASH_BUILD_FIRST, solutionSetIn, worksetIn);
		
		// NOTES ON THE ENUMERATION OF THE STEP FUNCTION PLANS:
		// Whenever we instantiate the iteration, we enumerate new candidates for the step function.
		// That way, we make sure we have an appropriate plan for each candidate for the initial partial solution,
		// we have a fitting candidate for the step function (often, work is pushed out of the step function).
		// Among the candidates of the step function, we keep only those that meet the requested properties of the
		// current candidate initial partial solution. That makes sure these properties exist at the beginning of
		// every iteration.
		
		// 1) Because we enumerate multiple times, we may need to clean the cached plans
		//    before starting another enumeration
		this.nextWorkset.accept(PlanCacheCleaner.INSTANCE);
		this.solutionSetDelta.accept(PlanCacheCleaner.INSTANCE);
		
		// 2) Give the partial solution the properties of the current candidate for the initial partial solution
		//    This concerns currently only the workset.
		this.worksetNode.setCandidateProperties(worksetIn.getGlobalProperties(), worksetIn.getLocalProperties(), worksetIn);
		this.solutionSetNode.setCandidateProperties(this.partitionedProperties, new LocalProperties(), solutionSetIn);
		
		final SolutionSetPlanNode sspn = this.solutionSetNode.getCurrentSolutionSetPlanNode();
		final WorksetPlanNode wspn = this.worksetNode.getCurrentWorksetPlanNode();
		
		// 3) Get the alternative plans
		List<PlanNode> solutionSetDeltaCandidates = this.solutionSetDelta.getAlternativePlans(estimator);
		List<PlanNode> worksetCandidates = this.nextWorkset.getAlternativePlans(estimator);
		
		// 4) Throw away all that are not compatible with the properties currently requested to the
		//    initial partial solution
		
		// Make sure that the workset candidates fulfill the input requirements
		{
			List<PlanNode> newCandidates = new ArrayList<PlanNode>();
			
			for (Iterator<PlanNode> planDeleter = worksetCandidates.iterator(); planDeleter.hasNext(); ) {
				PlanNode candidate = planDeleter.next();
				
				GlobalProperties atEndGlobal = candidate.getGlobalProperties();
				LocalProperties atEndLocal = candidate.getLocalProperties();
				
				FeedbackPropertiesMeetRequirementsReport report = candidate.checkPartialSolutionPropertiesMet(wspn,
																							atEndGlobal, atEndLocal);

				if (report == FeedbackPropertiesMeetRequirementsReport.NO_PARTIAL_SOLUTION) {
					; // depends only through broadcast variable on the workset solution
				}
				else if (report == FeedbackPropertiesMeetRequirementsReport.NOT_MET) {
					// attach a no-op node through which we create the properties of the original input
					Channel toNoOp = new Channel(candidate);
					globPropsReqWorkset.parameterizeChannel(toNoOp, false,
															nextWorksetRootConnection.getDataExchangeMode(), false);
					locPropsReqWorkset.parameterizeChannel(toNoOp);

					NoOpUnaryUdfOp noOpUnaryUdfOp = new NoOpUnaryUdfOp<>();
					noOpUnaryUdfOp.setInput(candidate.getProgramOperator());

					UnaryOperatorNode rebuildWorksetPropertiesNode = new UnaryOperatorNode(
						"Rebuild Workset Properties",
						noOpUnaryUdfOp,
						true);
					
					rebuildWorksetPropertiesNode.setParallelism(candidate.getParallelism());
					
					SingleInputPlanNode rebuildWorksetPropertiesPlanNode = new SingleInputPlanNode(
												rebuildWorksetPropertiesNode, "Rebuild Workset Properties",
												toNoOp, DriverStrategy.UNARY_NO_OP);
					rebuildWorksetPropertiesPlanNode.initProperties(toNoOp.getGlobalProperties(),
																	toNoOp.getLocalProperties());
					estimator.costOperator(rebuildWorksetPropertiesPlanNode);
						
					GlobalProperties atEndGlobalModified = rebuildWorksetPropertiesPlanNode.getGlobalProperties();
					LocalProperties atEndLocalModified = rebuildWorksetPropertiesPlanNode.getLocalProperties();
						
					if (!(atEndGlobalModified.equals(atEndGlobal) && atEndLocalModified.equals(atEndLocal))) {
						FeedbackPropertiesMeetRequirementsReport report2 = candidate.checkPartialSolutionPropertiesMet(
																		wspn, atEndGlobalModified, atEndLocalModified);
						if (report2 != FeedbackPropertiesMeetRequirementsReport.NOT_MET) {
							newCandidates.add(rebuildWorksetPropertiesPlanNode);
						}
					}
					
					// remove the original operator and add the modified candidate
					planDeleter.remove();
					
				}
			}
			
			worksetCandidates.addAll(newCandidates);
		}
		
		if (worksetCandidates.isEmpty()) {
			return;
		}
		
		// sanity check the solution set delta
		for (PlanNode solutionSetDeltaCandidate : solutionSetDeltaCandidates) {
			SingleInputPlanNode candidate = (SingleInputPlanNode) solutionSetDeltaCandidate;
			GlobalProperties gp = candidate.getGlobalProperties();

			if (gp.getPartitioning() != PartitioningProperty.HASH_PARTITIONED || gp.getPartitioningFields() == null ||
					!gp.getPartitioningFields().equals(this.solutionSetKeyFields)) {
				throw new CompilerException("Bug: The solution set delta is not partitioned.");
			}
		}
		
		// 5) Create a candidate for the Iteration Node for every remaining plan of the step function.
		
		final GlobalProperties gp = new GlobalProperties();
		gp.setHashPartitioned(this.solutionSetKeyFields);
		gp.addUniqueFieldCombination(this.solutionSetKeyFields);
		
		LocalProperties lp = LocalProperties.EMPTY.addUniqueFields(this.solutionSetKeyFields);
		
		// take all combinations of solution set delta and workset plans
		for (PlanNode solutionSetCandidate : solutionSetDeltaCandidates) {
			for (PlanNode worksetCandidate : worksetCandidates) {
				// check whether they have the same operator at their latest branching point
				if (this.singleRoot.areBranchCompatible(solutionSetCandidate, worksetCandidate)) {
					
					SingleInputPlanNode siSolutionDeltaCandidate = (SingleInputPlanNode) solutionSetCandidate;
					boolean immediateDeltaUpdate;
					
					// check whether we need a dedicated solution set delta operator, or whether we can update on the fly
					if (siSolutionDeltaCandidate.getInput().getShipStrategy() == ShipStrategyType.FORWARD &&
							this.solutionDeltaImmediatelyAfterSolutionJoin)
					{
						// we do not need this extra node. we can make the predecessor the delta
						// sanity check the node and connection
						if (siSolutionDeltaCandidate.getDriverStrategy() != DriverStrategy.UNARY_NO_OP ||
								siSolutionDeltaCandidate.getInput().getLocalStrategy() != LocalStrategy.NONE)
						{
							throw new CompilerException("Invalid Solution set delta node.");
						}
						
						solutionSetCandidate = siSolutionDeltaCandidate.getInput().getSource();
						immediateDeltaUpdate = true;
					} else {
						// was not partitioned, we need to keep this node.
						// mark that we materialize the input
						siSolutionDeltaCandidate.getInput().setTempMode(TempMode.PIPELINE_BREAKER);
						immediateDeltaUpdate = false;
					}
					
					WorksetIterationPlanNode wsNode = new WorksetIterationPlanNode(this,
							this.getOperator().getName(), solutionSetIn,
							worksetIn, sspn, wspn, worksetCandidate, solutionSetCandidate);
					wsNode.setImmediateSolutionSetUpdate(immediateDeltaUpdate);
					wsNode.initProperties(gp, lp);
					target.add(wsNode);
				}
			}
		}
	}
