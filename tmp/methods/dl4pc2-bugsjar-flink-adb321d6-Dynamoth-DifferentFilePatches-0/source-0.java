	public void postVisit(OptimizerNode node) {
		if (node instanceof IterationNode) {
			((IterationNode) node).acceptForStepFunction(this);
		}

		node.computeUnclosedBranchStack();
	}
