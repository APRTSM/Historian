    public RealPointValuePair doOptimize()
        throws OptimizationException {
        final SimplexTableau tableau =
            new SimplexTableau(f, constraints, goalType, restrictToNonNegative, epsilon);
        solvePhase1(tableau);
        tableau.discardArtificialVariables();
        while (!isOptimal(tableau)) {
            doIteration(tableau);
        }
        return tableau.getSolution();
    }
    private Integer getPivotColumn(SimplexTableau tableau) {
        double minValue = 0;
        Integer minPos = null;
        for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getWidth() - 1; i++) {
            if (MathUtils.compareTo(tableau.getEntry(0, i), minValue, epsilon) < 0) {
                minValue = tableau.getEntry(0, i);
                minPos = i;
            }
        }
        return minPos;
    }
