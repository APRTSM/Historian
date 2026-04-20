    private void checkParameters() {
        final double[] init = getStartPoint();
        final double[] lB = getLowerBound();
        final double[] uB = getUpperBound();

        // Convert API to internal handling of boundaries.
        boundaries = new double[2][];
        boundaries[0] = lB;
        boundaries[1] = uB;

        if (inputSigma != null) {
            if (inputSigma.length != init.length) {
                throw new DimensionMismatchException(inputSigma.length, init.length);
            }
            for (int i = 0; i < init.length; i++) {
                if (inputSigma[i] < 0) {
                    throw new NotPositiveException(inputSigma[i]);
                }
                if (inputSigma[i] > boundaries[1][i] - boundaries[0][i]) {
                    throw new OutOfRangeException(inputSigma[i], 0, boundaries[1][i] - boundaries[0][i]);
                }
            }
        }
    }
    private void initializeCMA(double[] guess) {
        if (lambda <= 0) {
            lambda = 4 + (int) (3. * Math.log(dimension));
        }
        // initialize sigma
        double[][] sigmaArray = new double[guess.length][1];
        for (int i = 0; i < guess.length; i++) {
            sigmaArray[i][0] = inputSigma == null ? 0.3 : inputSigma[i];
        }
        RealMatrix insigma = new Array2DRowRealMatrix(sigmaArray, false);
        sigma = max(insigma); // overall standard deviation

        // initialize termination criteria
        stopTolUpX = 1e3 * max(insigma);
        stopTolX = 1e-11 * max(insigma);
        stopTolFun = 1e-12;
        stopTolHistFun = 1e-13;

        // initialize selection strategy parameters
        mu = lambda / 2; // number of parents/points for recombination
        logMu2 = Math.log(mu + 0.5);
        weights = log(sequence(1, mu, 1)).scalarMultiply(-1.).scalarAdd(logMu2);
        double sumw = 0;
        double sumwq = 0;
        for (int i = 0; i < mu; i++) {
            double w = weights.getEntry(i, 0);
            sumw += w;
            sumwq += w * w;
        }
        weights = weights.scalarMultiply(1. / sumw);
        mueff = sumw * sumw / sumwq; // variance-effectiveness of sum w_i x_i

        // initialize dynamic strategy parameters and constants
        cc = (4. + mueff / dimension) /
                (dimension + 4. + 2. * mueff / dimension);
        cs = (mueff + 2.) / (dimension + mueff + 3.);
        damps = (1. + 2. * Math.max(0, Math.sqrt((mueff - 1.) /
                (dimension + 1.)) - 1.)) *
                Math.max(0.3, 1. - dimension /
                        (1e-6 + Math.min(maxIterations, getMaxEvaluations() /
                                lambda))) + cs; // minor increment
        ccov1 = 2. / ((dimension + 1.3) * (dimension + 1.3) + mueff);
        ccovmu = Math.min(1 - ccov1, 2. * (mueff - 2. + 1. / mueff) /
                ((dimension + 2.) * (dimension + 2.) + mueff));
        ccov1Sep = Math.min(1, ccov1 * (dimension + 1.5) / 3.);
        ccovmuSep = Math.min(1 - ccov1, ccovmu * (dimension + 1.5) / 3.);
        chiN = Math.sqrt(dimension) *
                (1. - 1. / (4. * dimension) + 1 / (21. * dimension * dimension));
        // intialize CMA internal values - updated each generation
        xmean = MatrixUtils.createColumnRealMatrix(guess); // objective
                                                           // variables
        diagD = insigma.scalarMultiply(1. / sigma);
        diagC = square(diagD);
        pc = zeros(dimension, 1); // evolution paths for C and sigma
        ps = zeros(dimension, 1); // B defines the coordinate system
        normps = ps.getFrobeniusNorm();

        B = eye(dimension, dimension);
        D = ones(dimension, 1); // diagonal D defines the scaling
        BD = times(B, repmat(diagD.transpose(), dimension, 1));
        C = B.multiply(diag(square(D)).multiply(B.transpose())); // covariance
        historySize = 10 + (int) (3. * 10. * dimension / lambda);
        fitnessHistory = new double[historySize]; // history of fitness values
        for (int i = 0; i < historySize; i++) {
            fitnessHistory[i] = Double.MAX_VALUE;
        }
    }
        public boolean isFeasible(final double[] x) {
            for (int i = 0; i < x.length; i++) {
                if (x[i] < boundaries[0][i]) {
                    return false;
                }
                if (x[i] > boundaries[1][i]) {
                    return false;
                }
            }
            return true;
        }
        private double[] repair(final double[] x) {
            final double[] repaired = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                if (x[i] < boundaries[0][i]) {
                    repaired[i] = boundaries[0][i];
                } else if (x[i] > boundaries[1][i]) {
                    repaired[i] = boundaries[1][i];
                } else {
                    repaired[i] = x[i];
                }
            }
            return repaired;
        }
        public double value(final double[] point) {
            double value;
            if (isRepairMode) {
                double[] repaired = repair(point);
                value = CMAESOptimizer.this
                        .computeObjectiveValue(repaired) +
                        penalty(point, repaired);
            } else {
                value = CMAESOptimizer.this
                        .computeObjectiveValue(point);
            }
            return isMinimize ? value : -value;
        }
    protected PointValuePair doOptimize() {
        checkParameters();
         // -------------------- Initialization --------------------------------
        isMinimize = getGoalType().equals(GoalType.MINIMIZE);
        final FitnessFunction fitfun = new FitnessFunction();
        final double[] guess = getStartPoint();
        // number of objective variables/problem dimension
        dimension = guess.length;
        initializeCMA(guess);
        iterations = 0;
        double bestValue = fitfun.value(guess);
        push(fitnessHistory, bestValue);
        PointValuePair optimum = new PointValuePair(getStartPoint(),
                isMinimize ? bestValue : -bestValue);
        PointValuePair lastResult = null;

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
                        if (diagonalOnly <= 0) {
                            arxk = xmean.add(BD.multiply(arz.getColumnMatrix(k))
                                    .scalarMultiply(sigma)); // m + sig * Normal(0,C)
                        } else {
                            arxk = xmean.add(times(diagD,arz.getColumnMatrix(k))
                                    .scalarMultiply(sigma));
                        }
                        if (i >= checkFeasableCount || fitfun.isFeasible(arxk.getColumn(0))) {
                            break;
                        }
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
                RealMatrix bestArx = selectColumns(arx, MathArrays.copyOf(arindex, mu));
                xmean = bestArx.multiply(weights);
                RealMatrix bestArz = selectColumns(arz, MathArrays.copyOf(arindex, mu));
                RealMatrix zmean = bestArz.multiply(weights);
                boolean hsig = updateEvolutionPaths(zmean, xold);
                if (diagonalOnly <= 0) {
                    updateCovariance(hsig, bestArx, arz, arindex, xold);
                } else {
                    updateCovarianceDiagonalOnly(hsig, bestArz, xold);
                }
                // Adapt step size sigma - Eq. (5)
                sigma *= Math.exp(Math.min(1.0,(normps/chiN - 1.)*cs/damps));
                double bestFitness = fitness[arindex[0]];
                double worstFitness = fitness[arindex[arindex.length-1]];
                if (bestValue > bestFitness) {
                    bestValue = bestFitness;
                    lastResult = optimum;
                    optimum = new PointValuePair(
                            fitfun.repair(bestArx.getColumn(0)),
                            isMinimize ? bestFitness : -bestFitness);
                    if (getConvergenceChecker() != null && lastResult != null) {
                        if (getConvergenceChecker().converged(iterations, optimum, lastResult)) {
                            break generationLoop;
                        }
                    }
                }
                // handle termination criteria
                // Break, if fitness is good enough
                if (stopFitness != 0) { // only if stopFitness is defined
                    if (bestFitness < (isMinimize ? stopFitness : -stopFitness)) {
                        break generationLoop;
                    }
                }
                double[] sqrtDiagC = sqrt(diagC).getColumn(0);
                double[] pcCol = pc.getColumn(0);
                for (int i = 0; i < dimension; i++) {
                    if (sigma*(Math.max(Math.abs(pcCol[i]), sqrtDiagC[i])) > stopTolX) {
                        break;
                    }
                    if (i >= dimension-1) {
                        break generationLoop;
                    }
                }
                for (int i = 0; i < dimension; i++) {
                    if (sigma*sqrtDiagC[i] > stopTolUpX) {
                        break generationLoop;
                    }
                }
                double historyBest = min(fitnessHistory);
                double historyWorst = max(fitnessHistory);
                if (iterations > 2 && Math.max(historyWorst, worstFitness) -
                        Math.min(historyBest, bestFitness) < stopTolFun) {
                    break generationLoop;
                }
                if (iterations > fitnessHistory.length &&
                        historyWorst-historyBest < stopTolHistFun) {
                    break generationLoop;
                }
                // condition number of the covariance matrix exceeds 1e14
                if (max(diagD)/min(diagD) > 1e7) {
                    break generationLoop;
                }
                // user defined termination
                if (getConvergenceChecker() != null) {
                    PointValuePair current =
                        new PointValuePair(bestArx.getColumn(0),
                                isMinimize ? bestFitness : -bestFitness);
                    if (lastResult != null &&
                        getConvergenceChecker().converged(iterations, current, lastResult)) {
                        break generationLoop;
                    }
                    lastResult = current;
                }
                // Adjust step size in case of equal function values (flat fitness)
                if (bestValue == fitness[arindex[(int)(0.1+lambda/4.)]]) {
                    sigma = sigma * Math.exp(0.2+cs/damps);
                }
                if (iterations > 2 && Math.max(historyWorst, bestFitness) -
                        Math.min(historyBest, bestFitness) == 0) {
                    sigma = sigma * Math.exp(0.2+cs/damps);
                }
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
