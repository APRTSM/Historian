    private Integer getBasicRow(final int col, boolean ignoreObjectiveRows) {
        Integer row = null;
        int start = ignoreObjectiveRows ? getNumObjectiveFunctions() : 0;
        for (int i = start; i < getHeight(); i++) {
            if (MathUtils.equals(getEntry(i, col), 1.0, epsilon) && (row == null)) {
                row = i;
            } else if (!MathUtils.equals(getEntry(i, col), 0.0, epsilon)) {
                return null;
            }
        }
        return row;
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
