    protected final int getOriginalNumDecisionVariables() {
        this.numArtificialVariables = 0;
		return restrictToNonNegative ? numDecisionVariables : numDecisionVariables - 1;
    }
