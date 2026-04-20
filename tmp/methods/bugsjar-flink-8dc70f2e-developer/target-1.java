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
