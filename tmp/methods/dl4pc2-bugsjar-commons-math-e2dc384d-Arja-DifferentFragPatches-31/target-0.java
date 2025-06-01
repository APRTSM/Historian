    public Optimum optimize(final LeastSquaresProblem problem) {
        //pull in relevant data from the problem as locals
        final int nR = problem.getObservationSize(); // Number of observed data.
        final int nC = problem.getParameterSize(); // Number of parameters.
        final double[] currentPoint = problem.getStart().toArray();
        //counters
        final Incrementor iterationCounter = problem.getIterationCounter();
        final Incrementor evaluationCounter = problem.getEvaluationCounter();
        //convergence criterion
        final ConvergenceChecker<Evaluation> checker
                = problem.getConvergenceChecker();

        // arrays shared with the other private methods
        final int solvedCols  = FastMath.min(nR, nC);
        /* Parameters evolution direction associated with lmPar. */
        double[] lmDir = new double[nC];
        /* Levenberg-Marquardt parameter. */
        double lmPar = 0;

        // local point
        double   delta   = 0;
        iterationCounter.incrementCount();
		double   xNorm   = 0;
        double[] diag    = new double[nC];
        double[] oldX    = new double[nC];
        double[] oldRes  = new double[nR];
        double[] qtf     = new double[nR];
        double[] work1   = new double[nC];
        double[] work2   = new double[nC];
        double[] work3   = new double[nC];


        for (int k = 0; k < nC; ++k) {
			double xK = diag[k] * currentPoint[k];
			xNorm += xK * xK;
		}
        //value will be reassigned in the loop
        Evaluation current = problem.evaluate(new ArrayRealVector(currentPoint));
        double[] currentResiduals = current.getResiduals().toArray();
        double currentCost = current.getCost();

        // Outer loop.
        boolean firstIteration = true;
        while (true) {
            iterationCounter.incrementCount();

            final Evaluation previous = current;

            // QR decomposition of the jacobian matrix
            final InternalData internalData
                    = qrDecomposition(current.getJacobian(), solvedCols);
            final double[][] weightedJacobian = internalData.weightedJacobian;
            final int[] permutation = internalData.permutation;
            final double[] diagR = internalData.diagR;
            final double[] jacNorm = internalData.jacNorm;

            //residuals already have weights applied
            double[] weightedResidual = currentResiduals;
            for (int i = 0; i < nR; i++) {
                qtf[i] = weightedResidual[i];
            }

            // compute Qt.res
            qTy(qtf, internalData);

            // now we don't need Q anymore,
            // so let jacobian contain the R matrix with its diagonal elements
            for (int k = 0; k < solvedCols; ++k) {
                int pk = permutation[k];
                weightedJacobian[k][pk] = diagR[pk];
            }

            if (firstIteration) {
                // scale the point according to the norms of the columns
                // of the initial jacobian
                xNorm = 0;
                for (int k = 0; k < nC; ++k) {
                    double dk = jacNorm[k];
                    if (dk == 0) {
                        dk = 1.0;
                    }
                    double xk = dk * currentPoint[k];
                    xNorm  += xk * xk;
                    diag[k] = dk;
                }
                xNorm = FastMath.sqrt(xNorm);

                // initialize the step bound delta
                delta = (xNorm == 0) ? initialStepBoundFactor : (initialStepBoundFactor * xNorm);
            }

            // check orthogonality between function vector and jacobian columns
            double maxCosine = 0;
            if (currentCost != 0) {
                for (int j = 0; j < solvedCols; ++j) {
                    int    pj = permutation[j];
                    double s  = jacNorm[pj];
                    if (s != 0) {
                        double sum = 0;
                        for (int i = 0; i <= j; ++i) {
                            sum += weightedJacobian[i][pj] * qtf[i];
                        }
                        maxCosine = FastMath.max(maxCosine, FastMath.abs(sum) / (s * currentCost));
                    }
                }
            }
            if (maxCosine <= orthoTolerance) {
                // Convergence has been reached.
                return new OptimumImpl(
                        current,
                        evaluationCounter.getCount(),
                        iterationCounter.getCount());
            }

            // rescale if necessary
            for (int j = 0; j < nC; ++j) {
                diag[j] = FastMath.max(diag[j], jacNorm[j]);
            }

            // Inner loop.
            for (double ratio = 0; ratio < 1.0e-4;) {

                // save the state
                for (int j = 0; j < solvedCols; ++j) {
                    int pj = permutation[j];
                    oldX[pj] = currentPoint[pj];
                }
                final double previousCost = currentCost;
                double[] tmpVec = weightedResidual;
                weightedResidual = oldRes;
                oldRes    = tmpVec;

                // determine the Levenberg-Marquardt parameter
                lmPar = determineLMParameter(qtf, delta, diag,
                                     internalData, solvedCols,
                                     work1, work2, work3, lmDir, lmPar);

                // compute the new point and the norm of the evolution direction
                double lmNorm = 0;
                for (int j = 0; j < solvedCols; ++j) {
                    int pj = permutation[j];
                    lmDir[pj] = -lmDir[pj];
                    currentPoint[pj] = oldX[pj] + lmDir[pj];
                    double s = diag[pj] * lmDir[pj];
                    lmNorm  += s * s;
                }
                lmNorm = FastMath.sqrt(lmNorm);
                // on the first iteration, adjust the initial step bound.
                if (firstIteration) {
                    delta = FastMath.min(delta, lmNorm);
                }

                // Evaluate the function at x + p and calculate its norm.
                evaluationCounter.incrementCount();
                current = problem.evaluate(new ArrayRealVector(currentPoint));
                currentResiduals = current.getResiduals().toArray();
                currentCost = current.getCost();

                // compute the scaled actual reduction
                double actRed = -1.0;
                if (0.1 * currentCost < previousCost) {
                    double r = currentCost / previousCost;
                    actRed = 1.0 - r * r;
                }

                // compute the scaled predicted reduction
                // and the scaled directional derivative
                for (int j = 0; j < solvedCols; ++j) {
                    int pj = permutation[j];
                    double dirJ = lmDir[pj];
                    work1[j] = 0;
                    for (int i = 0; i <= j; ++i) {
                        work1[i] += weightedJacobian[i][pj] * dirJ;
                    }
                }
                double coeff1 = 0;
                for (int j = 0; j < solvedCols; ++j) {
                    coeff1 += work1[j] * work1[j];
                }
                double pc2 = previousCost * previousCost;
                coeff1 /= pc2;
                double coeff2 = lmPar * lmNorm * lmNorm / pc2;
                double preRed = coeff1 + 2 * coeff2;
                double dirDer = -(coeff1 + coeff2);

                // ratio of the actual to the predicted reduction
                ratio = (preRed == 0) ? 0 : (actRed / preRed);

                // update the step bound
                if (ratio <= 0.25) {
                    double tmp =
                        (actRed < 0) ? (0.5 * dirDer / (dirDer + 0.5 * actRed)) : 0.5;
                        if ((0.1 * currentCost >= previousCost) || (tmp < 0.1)) {
                            tmp = 0.1;
                        }
                        delta = tmp * FastMath.min(delta, 10.0 * lmNorm);
                        lmPar /= tmp;
                } else if ((lmPar == 0) || (ratio >= 0.75)) {
                    delta = 2 * lmNorm;
                    lmPar *= 0.5;
                }

                // test for successful iteration.
                if (ratio >= 1.0e-4) {
                    // successful iteration, update the norm
                    firstIteration = false;
                    xNorm = 0;
                    for (int k = 0; k < nC; ++k) {
                        double xK = diag[k] * currentPoint[k];
                        xNorm += xK * xK;
                    }
                    xNorm = FastMath.sqrt(xNorm);

                    // tests for convergence.
                    if (checker != null && checker.converged(iterationCounter.getCount(), previous, current)) {
                        return new OptimumImpl(current, iterationCounter.getCount(), evaluationCounter.getCount());
                    }
                } else {
                    // failed iteration, reset the previous values
                    currentCost = previousCost;
                    for (int j = 0; j < solvedCols; ++j) {
                        int pj = permutation[j];
                        currentPoint[pj] = oldX[pj];
                    }
                    tmpVec    = weightedResidual;
                    weightedResidual = oldRes;
                    oldRes    = tmpVec;
                    // Reset "current" to previous values.
                    current = previous;
                }

                // Default convergence criteria.
                if ((FastMath.abs(actRed) <= costRelativeTolerance &&
                     preRed <= costRelativeTolerance &&
                     ratio <= 2.0) ||
                    delta <= parRelativeTolerance * xNorm) {
                    return new OptimumImpl(current, iterationCounter.getCount(), evaluationCounter.getCount());
                }

                // tests for termination and stringent tolerances
                if (FastMath.abs(actRed) <= TWO_EPS &&
                    preRed <= TWO_EPS &&
                    ratio <= 2.0) {
                    throw new ConvergenceException(LocalizedFormats.TOO_SMALL_COST_RELATIVE_TOLERANCE,
                                                   costRelativeTolerance);
                } else if (delta <= TWO_EPS * xNorm) {
                    throw new ConvergenceException(LocalizedFormats.TOO_SMALL_PARAMETERS_RELATIVE_TOLERANCE,
                                                   parRelativeTolerance);
                } else if (maxCosine <= TWO_EPS) {
                    throw new ConvergenceException(LocalizedFormats.TOO_SMALL_ORTHOGONALITY_TOLERANCE,
                                                   orthoTolerance);
                }
            }
        }
    }
