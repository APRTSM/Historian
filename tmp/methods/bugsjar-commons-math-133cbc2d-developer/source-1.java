    private Integer getPivotRow(SimplexTableau tableau, final int col) {
        // create a list of all the rows that tie for the lowest score in the minimum ratio test
        List<Integer> minRatioPositions = new ArrayList<Integer>();
        double minRatio = Double.MAX_VALUE;
        for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getHeight(); i++) {
            final double rhs = tableau.getEntry(i, tableau.getWidth() - 1);
            final double entry = tableau.getEntry(i, col);
            if (MathUtils.compareTo(entry, 0, epsilon) > 0) {
                final double ratio = rhs / entry;
                if (MathUtils.equals(ratio, minRatio, epsilon)) {
                    minRatioPositions.add(i);
                } else if (ratio < minRatio) {
                    minRatio = ratio;
                    minRatioPositions = new ArrayList<Integer>();
                    minRatioPositions.add(i);
                }
            }
        }

        if (minRatioPositions.size() == 0) {
          return null;
        } else if (minRatioPositions.size() > 1) {
          // there's a degeneracy as indicated by a tie in the minimum ratio test
          // check if there's an artificial variable that can be forced out of the basis
          for (Integer row : minRatioPositions) {
            for (int i = 0; i < tableau.getNumArtificialVariables(); i++) {
              int column = i + tableau.getArtificialVariableOffset();
              if (MathUtils.equals(tableau.getEntry(row, column), 1, epsilon) &&
                  row.equals(tableau.getBasicRow(column))) {
                return row;
              }
            }
          }
        }
        return minRatioPositions.get(0);
    }
    public SimplexSolver(final double epsilon) {
        this.epsilon = epsilon;
    }
    protected void solvePhase1(final SimplexTableau tableau) throws OptimizationException {

        // make sure we're in Phase 1
        if (tableau.getNumArtificialVariables() == 0) {
            return;
        }

        while (!tableau.isOptimal()) {
            doIteration(tableau);
        }

        // if W is not zero then we have no feasible solution
        if (!MathUtils.equals(tableau.getEntry(0, tableau.getRhsOffset()), 0, epsilon)) {
            throw new NoFeasibleSolutionException();
        }
    }
    public SimplexSolver() {
        this(DEFAULT_EPSILON);
    }
    public RealPointValuePair doOptimize() throws OptimizationException {
        final SimplexTableau tableau =
            new SimplexTableau(function, linearConstraints, goal, nonNegative, epsilon);

        solvePhase1(tableau);
        tableau.dropPhase1Objective();

        while (!tableau.isOptimal()) {
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
    protected static double getInvertedCoeffiecientSum(final RealVector coefficients) {
        double sum = 0;
        for (double coefficient : coefficients.getData()) {
            sum -= coefficient;
        }
        return sum;
    }
    public int hashCode() {
        return Boolean.valueOf(restrictToNonNegative).hashCode() ^
               numDecisionVariables ^
               numSlackVariables ^
               numArtificialVariables ^
               Double.valueOf(epsilon).hashCode() ^
               f.hashCode() ^
               constraints.hashCode() ^
               tableau.hashCode();
    }
    protected RealMatrix createTableau(final boolean maximize) {

        // create a matrix of the correct size
        int width = numDecisionVariables + numSlackVariables +
        numArtificialVariables + getNumObjectiveFunctions() + 1; // + 1 is for RHS
        int height = constraints.size() + getNumObjectiveFunctions();
        Array2DRowRealMatrix matrix = new Array2DRowRealMatrix(height, width);

        // initialize the objective function rows
        if (getNumObjectiveFunctions() == 2) {
            matrix.setEntry(0, 0, -1);
        }
        int zIndex = (getNumObjectiveFunctions() == 1) ? 0 : 1;
        matrix.setEntry(zIndex, zIndex, maximize ? 1 : -1);
        RealVector objectiveCoefficients =
            maximize ? f.getCoefficients().mapMultiply(-1) : f.getCoefficients();
        copyArray(objectiveCoefficients.getData(), matrix.getDataRef()[zIndex]);
        matrix.setEntry(zIndex, width - 1,
            maximize ? f.getConstantTerm() : -1 * f.getConstantTerm());

        if (!restrictToNonNegative) {
            matrix.setEntry(zIndex, getSlackVariableOffset() - 1,
                getInvertedCoeffiecientSum(objectiveCoefficients));
        }

        // initialize the constraint rows
        int slackVar = 0;
        int artificialVar = 0;
        for (int i = 0; i < constraints.size(); i++) {
            LinearConstraint constraint = constraints.get(i);
            int row = getNumObjectiveFunctions() + i;

            // decision variable coefficients
            copyArray(constraint.getCoefficients().getData(), matrix.getDataRef()[row]);

            // x-
            if (!restrictToNonNegative) {
                matrix.setEntry(row, getSlackVariableOffset() - 1,
                    getInvertedCoeffiecientSum(constraint.getCoefficients()));
            }

            // RHS
            matrix.setEntry(row, width - 1, constraint.getValue());

            // slack variables
            if (constraint.getRelationship() == Relationship.LEQ) {
                matrix.setEntry(row, getSlackVariableOffset() + slackVar++, 1);  // slack
            } else if (constraint.getRelationship() == Relationship.GEQ) {
                matrix.setEntry(row, getSlackVariableOffset() + slackVar++, -1); // excess
            }

            // artificial variables
            if ((constraint.getRelationship() == Relationship.EQ) ||
                    (constraint.getRelationship() == Relationship.GEQ)) {
                matrix.setEntry(0, getArtificialVariableOffset() + artificialVar, 1);
                matrix.setEntry(row, getArtificialVariableOffset() + artificialVar++, 1);
                matrix.setRowVector(0, matrix.getRowVector(0).subtract(matrix.getRowVector(row)));
            }
        }

        return matrix;
    }
    boolean isOptimal() {
        for (int i = getNumObjectiveFunctions(); i < getWidth() - 1; i++) {
            if (MathUtils.compareTo(tableau.getEntry(0, i), 0, epsilon) < 0) {
                return false;
            }
        }
        return true;
    }
    protected void dropPhase1Objective() {
        if (getNumObjectiveFunctions() == 1) {
            return;
        }

        List<Integer> columnsToDrop = new ArrayList<Integer>();
        columnsToDrop.add(0);

        // positive cost non-artificial variables
        for (int i = getNumObjectiveFunctions(); i < getArtificialVariableOffset(); i++) {
          if (MathUtils.compareTo(tableau.getEntry(0, i), 0, epsilon) > 0) {
            columnsToDrop.add(i);
          }
        }

        // non-basic artificial variables
        for (int i = 0; i < getNumArtificialVariables(); i++) {
          int col = i + getArtificialVariableOffset();
          if (getBasicRow(col) == null) {
            columnsToDrop.add(col);
          }
        }

        double[][] matrix = new double[getHeight() - 1][getWidth() - columnsToDrop.size()];
        for (int i = 1; i < getHeight(); i++) {
          int col = 0;
          for (int j = 0; j < getWidth(); j++) {
            if (!columnsToDrop.contains(j)) {
              matrix[i - 1][col++] = tableau.getEntry(i, j);
            }
          }
        }

        for (int i = columnsToDrop.size() - 1; i >= 0; i--) {
          columnLabels.remove((int) columnsToDrop.get(i));
        }

        this.tableau = new Array2DRowRealMatrix(matrix);
        this.numArtificialVariables = 0;
    }
    protected Integer getBasicRow(final int col) {
        Integer row = null;
        for (int i = 0; i < getHeight(); i++) {
            if (MathUtils.equals(getEntry(i, col), 1.0, epsilon) && (row == null)) {
                row = i;
            } else if (!MathUtils.equals(getEntry(i, col), 0.0, epsilon)) {
                return null;
            }
        }
        return row;
    }
    public boolean equals(Object other) {

      if (this == other) {
        return true;
      }

      if (other instanceof SimplexTableau) {
          SimplexTableau rhs = (SimplexTableau) other;
          return (restrictToNonNegative  == rhs.restrictToNonNegative) &&
                 (numDecisionVariables   == rhs.numDecisionVariables) &&
                 (numSlackVariables      == rhs.numSlackVariables) &&
                 (numArtificialVariables == rhs.numArtificialVariables) &&
                 (epsilon                == rhs.epsilon) &&
                 f.equals(rhs.f) &&
                 constraints.equals(rhs.constraints) &&
                 tableau.equals(rhs.tableau);
      }
      return false;
    }
    protected RealPointValuePair getSolution() {
      int negativeVarColumn = columnLabels.indexOf(NEGATIVE_VAR_COLUMN_LABEL);
      Integer negativeVarBasicRow = negativeVarColumn > 0 ? getBasicRow(negativeVarColumn) : null;
      double mostNegative = negativeVarBasicRow == null ? 0 : getEntry(negativeVarBasicRow, getRhsOffset());

      Set<Integer> basicRows = new HashSet<Integer>();
      double[] coefficients = new double[getOriginalNumDecisionVariables()];
      for (int i = 0; i < coefficients.length; i++) {
          int colIndex = columnLabels.indexOf("x" + i);
          if (colIndex < 0) {
            coefficients[i] = 0;
            continue;
          }
          Integer basicRow = getBasicRow(colIndex);
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
