	protected void openOperator() throws Exception {
		streamOperator.open(getTaskConfiguration());

		for (OneInputStreamOperator<?, ?> operator : outputHandler.chainedOperators) {
			operator.open(getTaskConfiguration());
		}
	}
