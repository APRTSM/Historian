  public double integrate(final FirstOrderDifferentialEquations equations,
                          final double t0, final double[] y0, final double t, final double[] y)
  throws DerivativeException, IntegratorException {

    sanityChecks(equations, t0, y0, t, y);
    setEquations(equations);
    resetEvaluations();
    final boolean forward = t > t0;

    // create some internal working arrays
    final double[] yDot0   = new double[y0.length];
    final double[] y1      = new double[y0.length];
    final double[] yTmp    = new double[y0.length];
    final double[] yTmpDot = new double[y0.length];

    final double[][] diagonal = new double[sequence.length-1][];
    final double[][] y1Diag = new double[sequence.length-1][];
    for (int k = 0; k < sequence.length-1; ++k) {
      diagonal[k] = new double[y0.length];
      y1Diag[k] = new double[y0.length];
    }

    final double[][][] fk  = new double[sequence.length][][];
    for (int k = 0; k < sequence.length; ++k) {

      fk[k]    = new double[sequence[k] + 1][];

      // all substeps start at the same point, so share the first array
      fk[k][0] = yDot0;

      for (int l = 0; l < sequence[k]; ++l) {
        fk[k][l+1] = new double[y0.length];
      }

    }

    if (y != y0) {
      System.arraycopy(y0, 0, y, 0, y0.length);
    }

    double[] yDot1      = null;
    double[][] yMidDots = null;
    if (denseOutput) {
      yDot1    = new double[y0.length];
      yMidDots = new double[1 + 2 * sequence.length][];
      for (int j = 0; j < yMidDots.length; ++j) {
        yMidDots[j] = new double[y0.length];
      }
    } else {
      yMidDots    = new double[1][];
      yMidDots[0] = new double[y0.length];
    }

    // initial scaling
    final double[] scale = new double[y0.length];
    rescale(y, y, scale);

    // initial order selection
    final double tol =
        (vecRelativeTolerance == null) ? scalRelativeTolerance : vecRelativeTolerance[0];
    final double log10R = Math.log(Math.max(1.0e-10, tol)) / Math.log(10.0);
    int targetIter = Math.max(1,
                              Math.min(sequence.length - 2,
                                       (int) Math.floor(0.5 - 0.6 * log10R)));
    // set up an interpolator sharing the integrator arrays
    AbstractStepInterpolator interpolator = null;
    if (denseOutput || (! eventsHandlersManager.isEmpty())) {
      interpolator = new GraggBulirschStoerStepInterpolator(y, yDot0,
                                                            y1, yDot1,
                                                            yMidDots, forward);
    } else {
      interpolator = new DummyStepInterpolator(y, forward);
    }
    interpolator.storeTime(t0);

    stepStart = t0;
    double  hNew             = 0;
    double  maxError         = Double.MAX_VALUE;
    boolean previousRejected = false;
    boolean firstTime        = true;
    boolean newStep          = true;
    boolean lastStep         = false;
    boolean firstStepAlreadyComputed = false;
    for (StepHandler handler : stepHandlers) {
        handler.reset();
    }
    costPerTimeUnit[0] = 0;
    while (! lastStep) {

      double error;
      boolean reject = false;

      if (newStep) {

        interpolator.shift();

        // first evaluation, at the beginning of the step
        if (! firstStepAlreadyComputed) {
          computeDerivatives(stepStart, y, yDot0);
        }

        if (firstTime) {

          hNew = initializeStep(equations, forward,
                                2 * targetIter + 1, scale,
                                stepStart, y, yDot0, yTmp, yTmpDot);

          if (! forward) {
            hNew = -hNew;
          }

        }

        newStep = false;

      }

      stepSize = hNew;

      // step adjustment near bounds
      if ((forward && (stepStart + stepSize > t)) ||
          ((! forward) && (stepStart + stepSize < t))) {
        stepSize = t - stepStart;
      }
      final double nextT = stepStart + stepSize;
      lastStep = forward ? (nextT >= t) : (nextT <= t);

      // iterate over several substep sizes
      int k = -1;
      for (boolean loop = true; loop; ) {

        ++k;

        // modified midpoint integration with the current substep
        if ( ! tryStep(stepStart, y, stepSize, k, scale, fk[k],
                       (k == 0) ? yMidDots[0] : diagonal[k-1],
                       (k == 0) ? y1 : y1Diag[k-1],
                       yTmp)) {

          // the stability check failed, we reduce the global step
          hNew   = Math.abs(filterStep(stepSize * stabilityReduction, forward, false));
          reject = true;
          loop   = false;

        } else {

          // the substep was computed successfully
          if (k > 0) {

            // extrapolate the state at the end of the step
            // using last iteration data
            extrapolate(0, k, y1Diag, y1);
            rescale(y, y1, scale);

            // estimate the error at the end of the step.
            error = 0;
            for (int j = 0; j < y0.length; ++j) {
              final double e = Math.abs(y1[j] - y1Diag[0][j]) / scale[j];
              error += e * e;
            }
            error = Math.sqrt(error / y0.length);

            if ((error > 1.0e15) || ((k > 1) && (error > maxError))) {
              // error is too big, we reduce the global step
              hNew   = Math.abs(filterStep(stepSize * stabilityReduction, forward, false));
              reject = true;
              loop   = false;
            } else {

              maxError = Math.max(4 * error, 1.0);

              // compute optimal stepsize for this order
              final double exp = 1.0 / (2 * k + 1);
              double fac = stepControl2 / Math.pow(error / stepControl1, exp);
              final double pow = Math.pow(stepControl3, exp);
              fac = Math.max(pow / stepControl4, Math.min(1 / pow, fac));
              optimalStep[k]     = Math.abs(filterStep(stepSize * fac, forward, true));
              costPerTimeUnit[k] = costPerStep[k] / optimalStep[k];

              // check convergence
              switch (k - targetIter) {

              case -1 :
                if ((targetIter > 1) && ! previousRejected) {

                  // check if we can stop iterations now
                  if (error <= 1.0) {
                    // convergence have been reached just before targetIter
                    loop = false;
                  } else {
                    // estimate if there is a chance convergence will
                    // be reached on next iteration, using the
                    // asymptotic evolution of error
                    final double ratio = ((double) sequence [k] * sequence[k+1]) /
                                         (sequence[0] * sequence[0]);
                    if (error > ratio * ratio) {
                      // we don't expect to converge on next iteration
                      // we reject the step immediately and reduce order
                      reject = true;
                      loop   = false;
                      targetIter = k;
                      if ((targetIter > 1) &&
                          (costPerTimeUnit[targetIter-1] <
                           orderControl1 * costPerTimeUnit[targetIter])) {
                        --targetIter;
                      }
                      hNew = optimalStep[targetIter];
                    }
                  }
                }
                break;

              case 0:
                if (error <= 1.0) {
                  // convergence has been reached exactly at targetIter
                  loop = false;
                } else {
                  // estimate if there is a chance convergence will
                  // be reached on next iteration, using the
                  // asymptotic evolution of error
                  final double ratio = ((double) sequence[k+1]) / sequence[0];
                  if (error > ratio * ratio) {
                    // we don't expect to converge on next iteration
                    // we reject the step immediately
                    reject = true;
                    loop = false;
                    if ((targetIter > 1) &&
                        (costPerTimeUnit[targetIter-1] <
                         orderControl1 * costPerTimeUnit[targetIter])) {
                      --targetIter;
                    }
                    hNew = optimalStep[targetIter];
                  }
                }
                break;

              case 1 :
                if (error > 1.0) {
                  reject = true;
                  if ((targetIter > 1) &&
                      (costPerTimeUnit[targetIter-1] <
                       orderControl1 * costPerTimeUnit[targetIter])) {
                    --targetIter;
                  }
                  hNew = optimalStep[targetIter];
                }
                loop = false;
                break;

              default :
                if ((firstTime || lastStep) && (error <= 1.0)) {
                  loop = false;
                }
                break;

              }

            }
          }
        }
      }

      // dense output handling
      double hInt = getMaxStep();
      if (denseOutput && ! reject) {

        // extrapolate state at middle point of the step
        for (int j = 1; j <= k; ++j) {
          extrapolate(0, j, diagonal, yMidDots[0]);
        }

        // derivative at end of step
        computeDerivatives(stepStart + stepSize, y1, yDot1);

        final int mu = 2 * k - mudif + 3;

        for (int l = 0; l < mu; ++l) {

          // derivative at middle point of the step
          final int l2 = l / 2;
          double factor = Math.pow(0.5 * sequence[l2], l);
          int middleIndex = fk[l2].length / 2;
          for (int i = 0; i < y0.length; ++i) {
            yMidDots[l+1][i] = factor * fk[l2][middleIndex + l][i];
          }
          for (int j = 1; j <= k - l2; ++j) {
            factor = Math.pow(0.5 * sequence[j + l2], l);
            middleIndex = fk[l2+j].length / 2;
            for (int i = 0; i < y0.length; ++i) {
              diagonal[j-1][i] = factor * fk[l2+j][middleIndex+l][i];
            }
            extrapolate(l2, j, diagonal, yMidDots[l+1]);
          }
          for (int i = 0; i < y0.length; ++i) {
            yMidDots[l+1][i] *= stepSize;
          }

          // compute centered differences to evaluate next derivatives
          for (int j = (l + 1) / 2; j <= k; ++j) {
            for (int m = fk[j].length - 1; m >= 2 * (l + 1); --m) {
              for (int i = 0; i < y0.length; ++i) {
                fk[j][m][i] -= fk[j][m-2][i];
              }
            }
          }

        }

        if (mu >= 0) {

          // estimate the dense output coefficients
          final GraggBulirschStoerStepInterpolator gbsInterpolator
            = (GraggBulirschStoerStepInterpolator) interpolator;
          gbsInterpolator.computeCoefficients(mu, stepSize);

          if (useInterpolationError) {
            // use the interpolation error to limit stepsize
            final double interpError = gbsInterpolator.estimateError(scale);
            hInt = Math.abs(stepSize / Math.max(Math.pow(interpError, 1.0 / (mu+4)),
                                                0.01));
            if (interpError > 10.0) {
              hNew = hInt;
              if ((!((org.apache.commons.math.ode.nonstiff.GraggBulirschStoerIntegrator.this.stepControl3 <= hInt) && (18 < interpError))) || (previousRejected)) {
                reject = true;
              }
            }
          }

          // Discrete events handling
          if (!reject) {
            interpolator.storeTime(stepStart + stepSize);
            if (eventsHandlersManager.evaluateStep(interpolator)) {
                final double dt = eventsHandlersManager.getEventTime() - stepStart;
                if (Math.abs(dt) > Math.ulp(stepStart)) {
                    // reject the step to match exactly the next switch time
                    hNew = Math.abs(dt);
                    reject = true;
                }
            }
          }

        }

        if (!reject) {
          // we will reuse the slope for the beginning of next step
          firstStepAlreadyComputed = true;
          System.arraycopy(yDot1, 0, yDot0, 0, y0.length);
        }

      }

      if (! reject) {

        // store end of step state
        final double nextStep = stepStart + stepSize;
        System.arraycopy(y1, 0, y, 0, y0.length);

        eventsHandlersManager.stepAccepted(nextStep, y);
        if (eventsHandlersManager.stop()) {
          lastStep = true;
        }

        // provide the step data to the step handler
        interpolator.storeTime(nextStep);
        for (StepHandler handler : stepHandlers) {
            handler.handleStep(interpolator, lastStep);
        }
        stepStart = nextStep;

        if (eventsHandlersManager.reset(stepStart, y) && ! lastStep) {
          // some switching function has triggered changes that
          // invalidate the derivatives, we need to recompute them
          firstStepAlreadyComputed = false;
        }

        int optimalIter;
        if (k == 1) {
          optimalIter = 2;
          if (previousRejected) {
            optimalIter = 1;
          }
        } else if (k <= targetIter) {
          optimalIter = k;
          if (costPerTimeUnit[k-1] < orderControl1 * costPerTimeUnit[k]) {
            optimalIter = k-1;
          } else if (costPerTimeUnit[k] < orderControl2 * costPerTimeUnit[k-1]) {
            optimalIter = Math.min(k+1, sequence.length - 2);
          }
        } else {
          optimalIter = k - 1;
          if ((k > 2) &&
              (costPerTimeUnit[k-2] < orderControl1 * costPerTimeUnit[k-1])) {
            optimalIter = k - 2;
          }
          if (costPerTimeUnit[k] < orderControl2 * costPerTimeUnit[optimalIter]) {
            optimalIter = Math.min(k, sequence.length - 2);
          }
        }

        if (previousRejected) {
          // after a rejected step neither order nor stepsize
          // should increase
          targetIter = Math.min(optimalIter, k);
          hNew = Math.min(Math.abs(stepSize), optimalStep[targetIter]);
        } else {
          // stepsize control
          if (optimalIter <= k) {
            hNew = optimalStep[optimalIter];
          } else {
            if ((k < targetIter) &&
                (costPerTimeUnit[k] < orderControl2 * costPerTimeUnit[k-1])) {
              hNew = filterStep(optimalStep[k] * costPerStep[optimalIter+1] / costPerStep[k],
                               forward, false);
            } else {
              hNew = filterStep(optimalStep[k] * costPerStep[optimalIter] / costPerStep[k],
                                forward, false);
            }
          }

          targetIter = optimalIter;

        }

        newStep = true;

      }

      hNew = Math.min(hNew, hInt);
      if (! forward) {
        hNew = -hNew;
      }

      firstTime = false;

      if (reject) {
        lastStep = false;
        previousRejected = true;
      } else {
        previousRejected = false;
      }

    }

    return stepStart;

  }
