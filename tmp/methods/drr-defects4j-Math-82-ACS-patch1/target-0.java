    private Integer getPivotRow(final int col, final SimplexTableau tableau) {
        double minRatio = Double.MAX_VALUE;
        Integer minRatioPos = null;
        for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getHeight(); i++) {
            final double rhs = tableau.getEntry(i, tableau.getWidth() - 1);
            final double entry = tableau.getEntry(i, col);
//ACS's patch begin
	if (MathUtils.compareTo(entry, 0, epsilon) >= 0&&!(MathUtils.compareTo(entry, 0, epsilon)==0.0)) {
//ACS's patch end
                final double ratio = rhs / entry;
                if (ratio < minRatio) {
                    minRatio = ratio;
                    minRatioPos = i; 
                }
            }

        }
        return minRatioPos;
    }
