    private void checkDimensions(final LinearObjectiveFunction f,
                                 final Collection<LinearConstraint> constraints) {
        final int dimension = f.getCoefficients().getDimension();
        for (final LinearConstraint constraint : constraints) {
            final int constraintDimension = constraint.getCoefficients().getDimension();
            if (constraintDimension != dimension) {
                throw new DimensionMismatchException(constraintDimension, dimension);
            }
        }
    }
    SimplexTableau(final LinearObjectiveFunction f,
                   final Collection<LinearConstraint> constraints,
                   final GoalType goalType,
                   final boolean restrictToNonNegative,
                   final double epsilon,
                   final int maxUlps) throws DimensionMismatchException {
        checkDimensions(f, constraints);
        this.f                      = f;
        this.constraints            = normalizeConstraints(constraints);
        this.restrictToNonNegative  = restrictToNonNegative;
        this.epsilon                = epsilon;
        this.maxUlps                = maxUlps;
        this.numDecisionVariables   = f.getCoefficients().getDimension() + (restrictToNonNegative ? 0 : 1);
        this.numSlackVariables      = getConstraintTypeCounts(Relationship.LEQ) +
                                      getConstraintTypeCounts(Relationship.GEQ);
        this.numArtificialVariables = getConstraintTypeCounts(Relationship.EQ) +
                                      getConstraintTypeCounts(Relationship.GEQ);
        this.tableau = createTableau(goalType == GoalType.MAXIMIZE);
        // initialize the basic variables for phase 1:
        //   we know that only slack or artificial variables can be basic
        initializeBasicVariables(getSlackVariableOffset());
        initializeColumnLabels();
    }
