    protected final int getOriginalNumDecisionVariables() {
        return f.getCoefficients().getDimension();
    }
    protected void initializeColumnLabels() {
      if (getNumObjectiveFunctions() == 2) {
        columnLabels.add("W");
      }
      columnLabels.add("Z");
      for (int i = 0; i < getOriginalNumDecisionVariables(); i++) {
        columnLabels.add("x" + i);
      }
      if (!restrictToNonNegative) {
        columnLabels.add("x-");
      }
      for (int i = 0; i < getNumSlackVariables(); i++) {
        columnLabels.add("s" + i);
      }
      for (int i = 0; i < getNumArtificialVariables(); i++) {
        columnLabels.add("a" + i);
      }
      columnLabels.add("RHS");
    }
    protected RealPointValuePair getSolution() {
      int negativeVarColumn = columnLabels.indexOf("x-");
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
    SimplexTableau(final LinearObjectiveFunction f,
                   final Collection<LinearConstraint> constraints,
                   final GoalType goalType, final boolean restrictToNonNegative,
                   final double epsilon) {
        this.f                      = f;
        this.constraints            = normalizeConstraints(constraints);
        this.restrictToNonNegative  = restrictToNonNegative;
        this.epsilon                = epsilon;
        this.numDecisionVariables   = f.getCoefficients().getDimension() +
                                      (restrictToNonNegative ? 0 : 1);
        this.numSlackVariables      = getConstraintTypeCounts(Relationship.LEQ) +
                                      getConstraintTypeCounts(Relationship.GEQ);
        this.numArtificialVariables = getConstraintTypeCounts(Relationship.EQ) +
                                      getConstraintTypeCounts(Relationship.GEQ);
        this.tableau = createTableau(goalType == GoalType.MAXIMIZE);
        initializeColumnLabels();
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
