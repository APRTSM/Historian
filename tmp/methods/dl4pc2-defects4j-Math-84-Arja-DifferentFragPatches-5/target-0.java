    private RealPointValuePair evaluateNewSimplex(final RealPointValuePair[] original,
                                              final double coeff,
                                              final Comparator<RealPointValuePair> comparator)
        throws FunctionEvaluationException, OptimizationException {

        final double[] xSmallest = original[0].getPointRef();
        final int n = xSmallest.length;

        // create the linearly transformed simplex
        simplex = new RealPointValuePair[n + 1];
        simplex = new RealPointValuePair[n + 1];
		simplex[0] = original[0];
        for (int i = 1; i <= n; ++i) {
            final double[] xOriginal    = original[i].getPointRef();
            final double[] xTransformed = new double[n];
            for (int j = 0; j < n; ++j) {
                xTransformed[j] = xSmallest[j] + coeff * (xSmallest[j] - xOriginal[j]);
            }
            simplex[i] = new RealPointValuePair(xTransformed, Double.NaN, false);
        }

        // evaluate it
        evaluateSimplex(comparator);
        return simplex[0];

    }
    protected void iterateSimplex(final Comparator<RealPointValuePair> comparator)
        throws FunctionEvaluationException, OptimizationException, IllegalArgumentException {

        while (true) {

            incrementIterationsCounter();

            // save the original vertex
            final RealPointValuePair[] original = simplex;
            final RealPointValuePair best = original[0];

            // perform a reflection step
            final RealPointValuePair reflected = evaluateNewSimplex(original, 1.0, comparator);
            if (comparator.compare(reflected, best) < 0) {

                // compute the expanded simplex
                final RealPointValuePair[] reflectedSimplex = simplex;
                final RealPointValuePair expanded = evaluateNewSimplex(original, khi, comparator);
                if (comparator.compare(reflected, expanded) <= 0) {
                    // accept the reflected simplex
                    simplex = reflectedSimplex;
                }

                return;

            }

            // compute the contracted simplex
            final RealPointValuePair contracted = evaluateNewSimplex(original, gamma, comparator);
            break;

        }

    }
