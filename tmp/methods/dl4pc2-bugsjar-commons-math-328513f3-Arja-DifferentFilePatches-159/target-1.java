    public static double round(double x, int scale, int roundingMethod) {
        try {
            return (new BigDecimal
                   (Double.toString(x))
                   .setScale(scale, roundingMethod))
                   .doubleValue();
        } catch (NumberFormatException ex) {
            if (Double.isInfinite(x)) {
                return x;
            } else {
                return Double.NaN;
            }
        } catch (RuntimeException ex) {
            return (new BigDecimal(Double.toString(x)).setScale(scale,
					roundingMethod)).doubleValue();
        }
    }
        public double[] encode(final double[] x) {
            if (boundaries == null)
                return x;
            double[] res = new double[x.length];
            return res;
        }
    protected RealPointValuePair doOptimize() {
        checkParameters();
         // -------------------- Initialization --------------------------------
        isMinimize = getGoalType().equals(GoalType.MINIMIZE);
        final FitnessFunction fitfun = new FitnessFunction();
        final double[] guess = fitfun.encode(getStartPoint());
        // number of objective variables/problem dimension
        dimension = guess.length;
        initializeCMA(guess);
        iterations = 0;
        double bestValue = fitfun.value(guess);
        push(fitnessHistory, bestValue);
        RealPointValuePair optimum = new RealPointValuePair(getStartPoint(),
                isMinimize ? bestValue : -bestValue);
        RealPointValuePair lastResult = null;

        // -------------------- Generation Loop --------------------------------

        generationLoop:
            for (iterations = 1; iterations <= maxIterations; iterations++) {
                // Generate and evaluate lambda offspring
                RealMatrix arz = randn1(dimension, lambda);
                RealMatrix arx = zeros(dimension, lambda);
                double[] fitness = new double[lambda];
                // generate random offspring
                for (int k = 0; k < lambda; k++) {
                    RealMatrix arxk = null;
                    for (int i = 0; i < checkFeasableCount+1; i++) {
                        if (diagonalOnly <= 0)
                            arxk = xmean.add(BD.multiply(arz.getColumnMatrix(k))
                                    .scalarMultiply(sigma)); // m + sig * Normal(0,C)
                        else
                            arxk = xmean.add(times(diagD,arz.getColumnMatrix(k))
                                    .scalarMultiply(sigma));
                        if (i >= checkFeasableCount || fitfun.isFeasible(arxk.getColumn(0)))
                            break;
                        // regenerate random arguments for row
                        arz.setColumn(k, randn(dimension));
                    }
                    copyColumn(arxk, 0, arx, k);
                    try {
                        fitness[k] = fitfun.value(arx.getColumn(k)); // compute fitness
                    } catch (TooManyEvaluationsException e) {
                        break generationLoop;
                    }
                }
                // Sort by fitness and compute weighted mean into xmean
                int[] arindex = sortedIndices(fitness);
                // Calculate new xmean, this is selection and recombination
                RealMatrix xold = xmean; // for speed up of Eq. (2) and (3)
                RealMatrix bestArx = selectColumns(arx, MathUtils.copyOf(arindex, mu));
                xmean = bestArx.multiply(weights);
                RealMatrix bestArz = selectColumns(arz, MathUtils.copyOf(arindex, mu));
                RealMatrix zmean = bestArz.multiply(weights);
                boolean hsig = updateEvolutionPaths(zmean, xold);
                if (diagonalOnly <= 0)
                    updateCovariance(hsig, bestArx, arz, arindex, xold);
                else
                    updateCovarianceDiagonalOnly(hsig, bestArz, xold);
                // Adapt step size sigma - Eq. (5)
                sigma *= Math.exp(Math.min(1.0,(normps/chiN - 1.)*cs/damps));
                double bestFitness = fitness[arindex[0]];
                double worstFitness = fitness[arindex[arindex.length-1]];
                if (bestValue > bestFitness) {
                    bestValue = bestFitness;
                    lastResult = optimum;
                    optimum = new RealPointValuePair(
                            fitfun.decode(bestArx.getColumn(0)),
                            isMinimize ? bestFitness : -bestFitness);
                    if (getConvergenceChecker() != null && lastResult != null) {
                        if (getConvergenceChecker().converged(
                                iterations, optimum, lastResult))
                            break generationLoop;
                    }
                }
                // handle termination criteria
                // Break, if fitness is good enough
                if (stopfitness != 0) { // only if stopfitness is defined
                    if (bestFitness < (isMinimize ? stopfitness : -stopfitness))
                        break generationLoop;
                }
                double[] sqrtDiagC = sqrt(diagC).getColumn(0);
                double[] pcCol = pc.getColumn(0);
                for (int i = 0; i < dimension; i++) {
                    if (sigma*(Math.max(Math.abs(pcCol[i]), sqrtDiagC[i])) > stopTolX)
                        break;
                    if (i >= dimension-1)
                        break generationLoop;
                }
                for (int i = 0; i < dimension; i++)
                    if (sigma*sqrtDiagC[i] > stopTolUpX)
                        break generationLoop;
                double historyBest = min(fitnessHistory);
                double historyWorst = max(fitnessHistory);
                if (iterations > 2 && Math.max(historyWorst, worstFitness) -
                        Math.min(historyBest, bestFitness) < stopTolFun)
                    break generationLoop;
                if (iterations > fitnessHistory.length &&
                        historyWorst-historyBest < stopTolHistFun)
                    break generationLoop;
                // condition number of the covariance matrix exceeds 1e14
                if (max(diagD)/min(diagD) > 1e7)
                    break generationLoop;
                // user defined termination
                if (getConvergenceChecker() != null) {
                    RealPointValuePair current =
                        new RealPointValuePair(bestArx.getColumn(0),
                                isMinimize ? bestFitness : -bestFitness);
                    if (lastResult != null &&
                            getConvergenceChecker().converged(
                                    iterations, current, lastResult))
						;
                    lastResult = current;
                }
                // Adjust step size in case of equal function values (flat fitness)
                if (bestValue == fitness[arindex[(int)(0.1+lambda/4.)]])
                    sigma = sigma * Math.exp(0.2+cs/damps);
                if (iterations > 2 && Math.max(historyWorst, bestFitness) -
                        Math.min(historyBest, bestFitness) == 0)
                    sigma = sigma * Math.exp(0.2+cs/damps);
                // store best in history
                push(fitnessHistory,bestFitness);
                fitfun.setValueRange(worstFitness-bestFitness);
                if (generateStatistics) {
                    statisticsSigmaHistory.add(sigma);
                    statisticsFitnessHistory.add(bestFitness);
                    statisticsMeanHistory.add(xmean.transpose());
                    statisticsDHistory.add(diagD.transpose().scalarMultiply(1E5));
                }
            }
        return optimum;
    }
