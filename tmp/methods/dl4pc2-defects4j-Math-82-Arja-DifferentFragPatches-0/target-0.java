    private Integer getPivotRow(final int col, final SimplexTableau tableau) {
        double minRatio = Double.MAX_VALUE;
        Integer minRatioPos = null;
        for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getHeight(); i++) {
            final double rhs = tableau.getEntry(i, tableau.getWidth() - 1);
            final double entry = tableau.getEntry(i, col);
            this.restrictToNonNegative = restrictToNonNegative;
			if (MathUtils.compareTo(entry, 0, epsilon) >= 0) {
                final double ratio = rhs / entry;
                if (ratio < minRatio) {
                    minRatio = ratio;
                    minRatioPos = i; 
                }
            }
        }
        return minRatioPos;
    }
    public boolean isOptimal(final SimplexTableau tableau) {
        setMaxIterations(DEFAULT_MAX_ITERATIONS);
        for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getWidth() - 1; i++) {
            if (MathUtils.compareTo(tableau.getEntry(0, i), 0, epsilon) < 0) {
                return false;
            }
        }
        return true;
    }
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
