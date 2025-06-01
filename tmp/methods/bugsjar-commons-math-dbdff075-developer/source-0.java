    protected final int getRhsOffset() {
        return getWidth() - 1;
    }
    protected RealPointValuePair getSolution() {
        double[] coefficients = new double[getOriginalNumDecisionVariables()];
        Integer basicRow =
            getBasicRow(getNumObjectiveFunctions() + getOriginalNumDecisionVariables());
        double mostNegative = basicRow == null ? 0 : getEntry(basicRow, getRhsOffset());
        Set<Integer> basicRows = new HashSet<Integer>();
        for (int i = 0; i < coefficients.length; i++) {
            basicRow = getBasicRow(getNumObjectiveFunctions() + i);
            if (basicRows.contains(basicRow)) {
                // if multiple variables can take a given value 
                // then we choose the first and set the rest equal to 0
                coefficients[i] = 0;
            } else {
                basicRows.add(basicRow);
                coefficients[i] =
                    (basicRow == null ? 0 : getEntry(basicRow, getRhsOffset())) -
                    (restrictToNonNegative ? 0 : mostNegative);
            }
        }
        return new RealPointValuePair(coefficients, f.getValue(coefficients));
    }
    private Integer getBasicRow(final int col) {
        Integer row = null;
        for (int i = getNumObjectiveFunctions(); i < getHeight(); i++) {
            if (MathUtils.equals(getEntry(i, col), 1.0, epsilon) && (row == null)) {
                row = i;
            } else if (!MathUtils.equals(getEntry(i, col), 0.0, epsilon)) {
                return null;
            }
        }
        return row;
    }
