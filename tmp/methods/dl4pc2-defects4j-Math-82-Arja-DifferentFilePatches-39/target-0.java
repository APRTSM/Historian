    protected final int getOriginalNumDecisionVariables() {
        return restrictToNonNegative ? numDecisionVariables
				: numDecisionVariables - 1;
    }
