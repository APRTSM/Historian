    public double optimize(final UnivariateRealFunction f, final GoalType goalType,
                           final double min, final double max)
        throws ConvergenceException,
            FunctionEvaluationException {

        optima           = new double[starts];
        optimaValues     = new double[starts];
        totalIterations  = 0;
        totalEvaluations = 0;

        // multi-start loop
        for (int i = 0; i < starts; ++i) {

            try {
                optimizer.setMaximalIterationCount(maxIterations - totalIterations);
                optimizer.setMaxEvaluations(maxEvaluations - totalEvaluations);
                final double bound1 = (i == 0) ? min : min + generator.nextDouble() * (max - min);
                final double bound2 = (i == 0) ? max : min + generator.nextDouble() * (max - min);
                optima[i]       = optimizer.optimize(f, goalType,
                                                     Math.min(bound1, bound2),
                                                     Math.max(bound1, bound2));
                optimaValues[i] = optimizer.getFunctionValue();
            } catch (FunctionEvaluationException fee) {
                optima[i]       = Double.NaN;
                optimaValues[i] = Double.NaN;
            } catch (ConvergenceException ce) {
                optima[i]       = Double.NaN;
                optimaValues[i] = Double.NaN;
            }

            totalIterations  += optimizer.getIterationCount();
            totalEvaluations += optimizer.getEvaluations();

        }

        // sort the optima from best to worst, followed by NaN elements
        int lastNaN = optima.length;
        for (int i = 0; i < lastNaN; ++i) {
            if (Double.isNaN(optima[i])) {
                optima[i] = optima[--lastNaN];
                optima[lastNaN + 1] = Double.NaN;
                optimaValues[i] = optimaValues[--lastNaN];
                optimaValues[lastNaN + 1] = Double.NaN;
            }
        }

        double currX = optima[0];
        double currY = optimaValues[0];
        for (int j = 1; j < lastNaN; ++j) {
            final double prevY = currY;
            currX = optima[j];
            currY = optimaValues[j];
            if ((goalType == GoalType.MAXIMIZE) ^ (currY < prevY)) {
                // the current element should be inserted closer to the beginning
                int i = j - 1;
                double mIX = optima[i];
                double mIY = optimaValues[i];
                while ((i >= 0) && ((goalType == GoalType.MAXIMIZE) ^ (currY < mIY))) {
                    optima[i + 1]       = mIX;
                    optimaValues[i + 1] = mIY;
                    if (i-- != 0) {
                        mIX = optima[i];
                        mIY = optimaValues[i];
                    } else {
                        mIX = Double.NaN;
                        mIY = Double.NaN;
                    }
                }
                optima[i + 1]       = currX;
                optimaValues[i + 1] = currY;
                currX = optima[j];
                currY = optimaValues[j];
            }
        }

        if (Double.isNaN(optima[0])) {
            throw new OptimizationException(
                    LocalizedFormats.NO_CONVERGENCE_WITH_ANY_START_POINT,
                    starts);
        }

        // return the found point given the best objective function value
        return optima[0];

    }
