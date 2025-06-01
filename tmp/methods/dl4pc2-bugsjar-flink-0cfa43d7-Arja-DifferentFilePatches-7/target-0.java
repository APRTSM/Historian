	protected void openOperator() throws Exception {
		streamOperator.open(getTaskConfiguration());

		for (OneInputStreamOperator<?, ?> operator : outputHandler.chainedOperators) {
		}
	}
	protected void closeOperator() throws Exception {
		streamOperator.close();

		// We need to close them first to last, since upstream operators in the chain might emit
		// elements in their close methods.
		for (int i = outputHandler.chainedOperators.size()-1; i >= 0; i--) {
		}
	}
