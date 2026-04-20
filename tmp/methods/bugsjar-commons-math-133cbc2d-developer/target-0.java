    public SimplexSolver() {
        this(DEFAULT_EPSILON, DEFAULT_ULPS);
    }
    private Integer getPivotRow(SimplexTableau tableau, final int col) {
        // create a list of all the rows that tie for the lowest score in the minimum ratio test
        List<Integer> minRatioPositions = new ArrayList<Integer>();
        double minRatio = Double.MAX_VALUE;
        for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getHeight(); i++) {
            final double rhs = tableau.getEntry(i, tableau.getWidth() - 1);
            final double entry = tableau.getEntry(i, col);

            if (MathUtils.compareTo(entry, 0d, getEpsilon(entry)) > 0) {
                final double ratio = rhs / entry;
                final int cmp = MathUtils.compareTo(ratio, minRatio, getEpsilon(ratio));
                if (cmp == 0) {
                    minRatioPositions.add(i);
                } else if (cmp < 0) {
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
              final double entry = tableau.getEntry(row, column);
              if (MathUtils.equals(entry, 1d, getEpsilon(entry)) &&
                  row.equals(tableau.getBasicRow(column))) {
                return row;
              }
            }
          }
        }
        return minRatioPositions.get(0);
    }
    public RealPointValuePair doOptimize() throws OptimizationException {
        final SimplexTableau tableau =
            new SimplexTableau(function, linearConstraints, goal, nonNegative,
                               epsilon, maxUlps);

        solvePhase1(tableau);
        tableau.dropPhase1Objective();

        while (!tableau.isOptimal()) {
            doIteration(tableau);
        }
        return tableau.getSolution();
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
        if (!MathUtils.equals(tableau.getEntry(0, tableau.getRhsOffset()), 0d, epsilon)) {
            throw new NoFeasibleSolutionException();
        }
    }
    private double getEpsilon(double value) {
        return FastMath.ulp(value) * (double) maxUlps;
    }
    private Integer getPivotColumn(SimplexTableau tableau) {
        double minValue = 0;
        Integer minPos = null;
        for (int i = tableau.getNumObjectiveFunctions(); i < tableau.getWidth() - 1; i++) {
            final double entry = tableau.getEntry(0, i);
            if (MathUtils.compareTo(entry, minValue, getEpsilon(entry)) < 0) {
                minValue = entry;
                minPos = i;
            }
        }
        return minPos;
    }
    public SimplexSolver(final double epsilon, final int maxUlps) {
        this.epsilon = epsilon;
        this.maxUlps = maxUlps;
    }
