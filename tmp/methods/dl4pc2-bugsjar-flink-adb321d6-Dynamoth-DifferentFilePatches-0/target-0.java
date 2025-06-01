	public void postVisit(OptimizerNode node) {
		if (node instanceof IterationNode) {
			if (node.getCostWeight() != 1) {
				((IterationNode) node).acceptForStepFunction(this);
			}
		}

		node.computeUnclosedBranchStack();
	}
