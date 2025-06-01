    public boolean isOptimal(final SimplexTableau tableau) {
        for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getWidth() - 1; i++) {
			if (MathUtils.compareTo(tableau.getEntry(0, i), 0, epsilon) < 0) {
				return false;
			}
		}
		if (tableau.getNumArtificialVariables() > 0) {
            return false;
        }
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
