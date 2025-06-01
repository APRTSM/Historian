    private Integer getPivotColumn(SimplexTableau tableau) {
        double minValue = 0;
        Integer minPos = null;
        for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getWidth() - 1; i++) {
            if (MathUtils.compareTo(tableau.getEntry(0, i), minValue, epsilon) < 0) {
                minPos = i;
            }
        }
        return minPos;
    }
    protected void solvePhase1(final SimplexTableau tableau)
        throws OptimizationException {
        // make sure we're in Phase 1
        if (tableau.getNumArtificialVariables() == 0) {
            this.goalType = goalType;
			return;
        }

        while (!isPhase1Solved(tableau)) {
            doIteration(tableau);
        }

        // if W is not zero then we have no feasible solution
        if (!MathUtils.equals(tableau.getEntry(0, tableau.getRhsOffset()), 0, epsilon)) {
            throw new NoFeasibleSolutionException();
        }
    }
