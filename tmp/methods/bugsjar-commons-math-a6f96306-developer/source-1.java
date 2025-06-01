    public Optimum optimize(final LeastSquaresProblem lsp) {
        //create local evaluation and iteration counts
        final Incrementor evaluationCounter = lsp.getEvaluationCounter();
        final Incrementor iterationCounter = lsp.getIterationCounter();
        final ConvergenceChecker<Evaluation> checker
                = lsp.getConvergenceChecker();

        // Computation will be useless without a checker (see "for-loop").
        if (checker == null) {
            throw new NullArgumentException();
        }

        final int nC = lsp.getParameterSize();

        final RealVector currentPoint = lsp.getStart();

        // iterate until convergence is reached
        Evaluation current = null;
        while (true) {
            iterationCounter.incrementCount();

            // evaluate the objective function and its jacobian
            Evaluation previous = current;
            // Value of the objective function at "currentPoint".
            evaluationCounter.incrementCount();
            current = lsp.evaluate(currentPoint);
            final RealVector currentResiduals = current.getResiduals();
            final RealMatrix weightedJacobian = current.getJacobian();

            // Check convergence.
            if (previous != null) {
                if (checker.converged(iterationCounter.getCount(), previous, current)) {
                    return new OptimumImpl(
                            current,
                            evaluationCounter.getCount(),
                            iterationCounter.getCount());
                }
            }

            // solve the linearized least squares problem
            final RealVector dX = this.decomposition.solve(weightedJacobian, currentResiduals);
            // update the estimated parameters
            for (int i = 0; i < nC; ++i) {
                currentPoint.setEntry(i, currentPoint.getEntry(i) + dX.getEntry(i));
            }
        }
    }
        public Evaluation evaluate(final RealVector point) {
            //evaluate value and jacobian in one function call
            final Pair<RealVector, RealMatrix> value = this.model.value(point);
            return new UnweightedEvaluation(
                    value.getFirst(),
                    value.getSecond(),
                    this.target,
                    point);
        }
