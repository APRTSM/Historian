    public void setStartConfiguration(final double[] steps)
        throws IllegalArgumentException {
        // only the relative position of the n final vertices with respect
        // to the first one are stored
        final int n = steps.length;
        startConfiguration = new double[n][n];
        for (int i = 0; i < n; ++i) {
            final double[] vertexI = startConfiguration[i];
            for (int j = 0; j < i + 1; ++j) {
                System.arraycopy(steps, 0, vertexI, 0, j + 1);
            }
        }
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
