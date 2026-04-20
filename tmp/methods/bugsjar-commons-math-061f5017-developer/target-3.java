    public double integrate(final FirstOrderDifferentialEquations equations,
                            final double t0, final double[] y0,
                            final double t, final double[] y)
        throws DerivativeException, IntegratorException {

        final int n = y0.length;
        sanityChecks(equations, t0, y0, t, y);
        setEquations(equations);
        resetEvaluations();
        final boolean forward = t > t0;

        // initialize working arrays
        if (y != y0) {
            System.arraycopy(y0, 0, y, 0, n);
        }
        final double[] yDot = new double[n];
        final double[] yTmp = new double[y0.length];

        // set up an interpolator sharing the integrator arrays
        final NordsieckStepInterpolator interpolator = new NordsieckStepInterpolator();
        interpolator.reinitialize(y, forward);
        final NordsieckStepInterpolator interpolatorTmp = new NordsieckStepInterpolator();
        interpolatorTmp.reinitialize(yTmp, forward);

        // set up integration control objects
        for (StepHandler handler : stepHandlers) {
            handler.reset();
        }
        CombinedEventsManager manager = addEndTimeChecker(t0, t, eventsHandlersManager);

        // compute the initial Nordsieck vector using the configured starter integrator
        start(t0, y, t);
        interpolator.reinitialize(stepStart, stepSize, scaled, nordsieck);
        interpolator.storeTime(stepStart);
        final int lastRow = nordsieck.getRowDimension() - 1;

        // reuse the step that was chosen by the starter integrator
        double hNew = stepSize;
        interpolator.rescale(hNew);

        boolean lastStep = false;
        while (!lastStep) {

            // shift all data
            interpolator.shift();

            double error = 0;
            for (boolean loop = true; loop;) {

                stepSize = hNew;

                // evaluate error using the last term of the Taylor expansion
                error = 0;
                for (int i = 0; i < y0.length; ++i) {
                    final double yScale = Math.abs(y[i]);
                    final double tol = (vecAbsoluteTolerance == null) ?
                                       (scalAbsoluteTolerance + scalRelativeTolerance * yScale) :
                                       (vecAbsoluteTolerance[i] + vecRelativeTolerance[i] * yScale);
                    final double ratio  = nordsieck.getEntry(lastRow, i) / tol;
                    error += ratio * ratio;
                }
                error = Math.sqrt(error / y0.length);

                if (error <= 1.0) {

                    // predict a first estimate of the state at step end
                    final double stepEnd = stepStart + stepSize;
                    interpolator.setInterpolatedTime(stepEnd);
                    System.arraycopy(interpolator.getInterpolatedState(), 0, yTmp, 0, y0.length);

                    // evaluate the derivative
                    computeDerivatives(stepEnd, yTmp, yDot);

                    // update Nordsieck vector
                    final double[] predictedScaled = new double[y0.length];
                    for (int j = 0; j < y0.length; ++j) {
                        predictedScaled[j] = stepSize * yDot[j];
                    }
                    final Array2DRowRealMatrix nordsieckTmp = updateHighOrderDerivativesPhase1(nordsieck);
                    updateHighOrderDerivativesPhase2(scaled, predictedScaled, nordsieckTmp);

                    // discrete events handling
                    interpolatorTmp.reinitialize(stepEnd, stepSize, predictedScaled, nordsieckTmp);
                    interpolatorTmp.storeTime(stepStart);
                    interpolatorTmp.shift();
                    interpolatorTmp.storeTime(stepEnd);
                    if (manager.evaluateStep(interpolatorTmp)) {
                        final double dt = manager.getEventTime() - stepStart;
                        if (Math.abs(dt) <= Math.ulp(stepStart)) {
                            // we cannot simply truncate the step, reject the current computation
                            // and let the loop compute another state with the truncated step.
                            // it is so small (much probably exactly 0 due to limited accuracy)
                            // that the code above would fail handling it.
                            // So we set up an artificial 0 size step by copying states
                            interpolator.storeTime(stepStart);
                            System.arraycopy(y, 0, yTmp, 0, y0.length);
                            hNew     = 0;
                            stepSize = 0;
                            loop     = false;
                        } else {
                            // reject the step to match exactly the next switch time
                            hNew = dt;
                            interpolator.rescale(hNew);
                        }
                    } else {
                        // accept the step
                        scaled    = predictedScaled;
                        nordsieck = nordsieckTmp;
                        interpolator.reinitialize(stepEnd, stepSize, scaled, nordsieck);
                        loop = false;
                    }

                } else {
                    // reject the step and attempt to reduce error by stepsize control
                    final double factor = computeStepGrowShrinkFactor(error);
                    hNew = filterStep(stepSize * factor, forward, false);
                    interpolator.rescale(hNew);
                }

            }

            // the step has been accepted (may have been truncated)
            final double nextStep = stepStart + stepSize;
            System.arraycopy(yTmp, 0, y, 0, n);
            interpolator.storeTime(nextStep);
            manager.stepAccepted(nextStep, y);
            lastStep = manager.stop();

            // provide the step data to the step handler
            for (StepHandler handler : stepHandlers) {
                interpolator.setInterpolatedTime(nextStep);
                handler.handleStep(interpolator, lastStep);
            }
            stepStart = nextStep;

            if (!lastStep && manager.reset(stepStart, y)) {

                // some events handler has triggered changes that
                // invalidate the derivatives, we need to restart from scratch
                start(stepStart, y, t);
                interpolator.reinitialize(stepStart, stepSize, scaled, nordsieck);

            }

            if (! lastStep) {
                // in some rare cases we may get here with stepSize = 0, for example
                // when an event occurs at integration start, reducing the first step
                // to zero; we have to reset the step to some safe non zero value
                stepSize = filterStep(stepSize, forward, true);

                // stepsize control for next step
                final double  factor     = computeStepGrowShrinkFactor(error);
                final double  scaledH    = stepSize * factor;
                final double  nextT      = stepStart + scaledH;
                final boolean nextIsLast = forward ? (nextT >= t) : (nextT <= t);
                hNew = filterStep(scaledH, forward, nextIsLast);
                interpolator.rescale(hNew);
            }

        }

        final double stopTime  = stepStart;
        stepStart = Double.NaN;
        stepSize  = Double.NaN;
        return stopTime;

    }
    public double integrate(final FirstOrderDifferentialEquations equations,
                            final double t0, final double[] y0,
                            final double t, final double[] y)
        throws DerivativeException, IntegratorException {

        final int n = y0.length;
        sanityChecks(equations, t0, y0, t, y);
        setEquations(equations);
        resetEvaluations();
        final boolean forward = t > t0;

        // initialize working arrays
        if (y != y0) {
            System.arraycopy(y0, 0, y, 0, n);
        }
        final double[] yDot = new double[y0.length];
        final double[] yTmp = new double[y0.length];

        // set up two interpolators sharing the integrator arrays
        final NordsieckStepInterpolator interpolator = new NordsieckStepInterpolator();
        interpolator.reinitialize(y, forward);
        final NordsieckStepInterpolator interpolatorTmp = new NordsieckStepInterpolator();
        interpolatorTmp.reinitialize(yTmp, forward);

        // set up integration control objects
        for (StepHandler handler : stepHandlers) {
            handler.reset();
        }
        CombinedEventsManager manager = addEndTimeChecker(t0, t, eventsHandlersManager);


        // compute the initial Nordsieck vector using the configured starter integrator
        start(t0, y, t);
        interpolator.reinitialize(stepStart, stepSize, scaled, nordsieck);
        interpolator.storeTime(stepStart);

        double hNew = stepSize;
        interpolator.rescale(hNew);

        boolean lastStep = false;
        while (!lastStep) {

            // shift all data
            interpolator.shift();

            double error = 0;
            for (boolean loop = true; loop;) {

                stepSize = hNew;

                // predict a first estimate of the state at step end (P in the PECE sequence)
                final double stepEnd = stepStart + stepSize;
                interpolator.setInterpolatedTime(stepEnd);
                System.arraycopy(interpolator.getInterpolatedState(), 0, yTmp, 0, y0.length);

                // evaluate a first estimate of the derivative (first E in the PECE sequence)
                computeDerivatives(stepEnd, yTmp, yDot);

                // update Nordsieck vector
                final double[] predictedScaled = new double[y0.length];
                for (int j = 0; j < y0.length; ++j) {
                    predictedScaled[j] = stepSize * yDot[j];
                }
                final Array2DRowRealMatrix nordsieckTmp = updateHighOrderDerivativesPhase1(nordsieck);
                updateHighOrderDerivativesPhase2(scaled, predictedScaled, nordsieckTmp);

                // apply correction (C in the PECE sequence)
                error = nordsieckTmp.walkInOptimizedOrder(new Corrector(y, predictedScaled, yTmp));

                if (error <= 1.0) {

                    // evaluate a final estimate of the derivative (second E in the PECE sequence)
                    computeDerivatives(stepEnd, yTmp, yDot);

                    // update Nordsieck vector
                    final double[] correctedScaled = new double[y0.length];
                    for (int j = 0; j < y0.length; ++j) {
                        correctedScaled[j] = stepSize * yDot[j];
                    }
                    updateHighOrderDerivativesPhase2(predictedScaled, correctedScaled, nordsieckTmp);

                    // discrete events handling
                    interpolatorTmp.reinitialize(stepEnd, stepSize, correctedScaled, nordsieckTmp);
                    interpolatorTmp.storeTime(stepStart);
                    interpolatorTmp.shift();
                    interpolatorTmp.storeTime(stepEnd);
                    if (manager.evaluateStep(interpolatorTmp)) {
                        final double dt = manager.getEventTime() - stepStart;
                        if (Math.abs(dt) <= Math.ulp(stepStart)) {
                            // we cannot simply truncate the step, reject the current computation
                            // and let the loop compute another state with the truncated step.
                            // it is so small (much probably exactly 0 due to limited accuracy)
                            // that the code above would fail handling it.
                            // So we set up an artificial 0 size step by copying states
                            interpolator.storeTime(stepStart);
                            System.arraycopy(y, 0, yTmp, 0, y0.length);
                            hNew     = 0;
                            stepSize = 0;
                            loop     = false;
                        } else {
                            // reject the step to match exactly the next switch time
                            hNew = dt;
                            interpolator.rescale(hNew);
                        }
                    } else {
                        // accept the step
                        scaled    = correctedScaled;
                        nordsieck = nordsieckTmp;
                        interpolator.reinitialize(stepEnd, stepSize, scaled, nordsieck);
                        loop = false;
                    }

                } else {
                    // reject the step and attempt to reduce error by stepsize control
                    final double factor = computeStepGrowShrinkFactor(error);
                    hNew = filterStep(stepSize * factor, forward, false);
                    interpolator.rescale(hNew);
                }

            }

            // the step has been accepted (may have been truncated)
            final double nextStep = stepStart + stepSize;
            System.arraycopy(yTmp, 0, y, 0, n);
            interpolator.storeTime(nextStep);
            manager.stepAccepted(nextStep, y);
            lastStep = manager.stop();

            // provide the step data to the step handler
            for (StepHandler handler : stepHandlers) {
                interpolator.setInterpolatedTime(nextStep);
                handler.handleStep(interpolator, lastStep);
            }
            stepStart = nextStep;

            if (!lastStep && manager.reset(stepStart, y)) {

                // some events handler has triggered changes that
                // invalidate the derivatives, we need to restart from scratch
                start(stepStart, y, t);
                interpolator.reinitialize(stepStart, stepSize, scaled, nordsieck);

            }

            if (! lastStep) {
                // in some rare cases we may get here with stepSize = 0, for example
                // when an event occurs at integration start, reducing the first step
                // to zero; we have to reset the step to some safe non zero value
                stepSize = filterStep(stepSize, forward, true);

                // stepsize control for next step
                final double  factor     = computeStepGrowShrinkFactor(error);
                final double  scaledH    = stepSize * factor;
                final double  nextT      = stepStart + scaledH;
                final boolean nextIsLast = forward ? (nextT >= t) : (nextT <= t);
                hNew = filterStep(scaledH, forward, nextIsLast);
                interpolator.rescale(hNew);
            }

        }

        final double stopTime  = stepStart;
        stepStart = Double.NaN;
        stepSize  = Double.NaN;
        return stopTime;

    }
  public double integrate(final FirstOrderDifferentialEquations equations,
                          final double t0, final double[] y0,
                          final double t, final double[] y)
  throws DerivativeException, IntegratorException {

    sanityChecks(equations, t0, y0, t, y);
    setEquations(equations);
    resetEvaluations();
    final boolean forward = t > t0;

    // create some internal working arrays
    final int stages = c.length + 1;
    if (y != y0) {
      System.arraycopy(y0, 0, y, 0, y0.length);
    }
    final double[][] yDotK = new double[stages][y0.length];
    final double[] yTmp = new double[y0.length];

    // set up an interpolator sharing the integrator arrays
    AbstractStepInterpolator interpolator;
    if (requiresDenseOutput() || (! eventsHandlersManager.isEmpty())) {
      final RungeKuttaStepInterpolator rki = (RungeKuttaStepInterpolator) prototype.copy();
      rki.reinitialize(this, yTmp, yDotK, forward);
      interpolator = rki;
    } else {
      interpolator = new DummyStepInterpolator(yTmp, yDotK[stages - 1], forward);
    }
    interpolator.storeTime(t0);

    // set up integration control objects
    stepStart         = t0;
    double  hNew      = 0;
    boolean firstTime = true;
    for (StepHandler handler : stepHandlers) {
        handler.reset();
    }
    CombinedEventsManager manager = addEndTimeChecker(t0, t, eventsHandlersManager);
    boolean lastStep = false;

    // main integration loop
    while (!lastStep) {

      interpolator.shift();

      double error = 0;
      for (boolean loop = true; loop;) {

        if (firstTime || !fsal) {
          // first stage
          computeDerivatives(stepStart, y, yDotK[0]);
        }

        if (firstTime) {
          final double[] scale = new double[y0.length];
          if (vecAbsoluteTolerance == null) {
              for (int i = 0; i < scale.length; ++i) {
                scale[i] = scalAbsoluteTolerance + scalRelativeTolerance * Math.abs(y[i]);
              }
            } else {
              for (int i = 0; i < scale.length; ++i) {
                scale[i] = vecAbsoluteTolerance[i] + vecRelativeTolerance[i] * Math.abs(y[i]);
              }
            }
          hNew = initializeStep(equations, forward, getOrder(), scale,
                                stepStart, y, yDotK[0], yTmp, yDotK[1]);
          firstTime = false;
        }

        stepSize = hNew;

        // next stages
        for (int k = 1; k < stages; ++k) {

          for (int j = 0; j < y0.length; ++j) {
            double sum = a[k-1][0] * yDotK[0][j];
            for (int l = 1; l < k; ++l) {
              sum += a[k-1][l] * yDotK[l][j];
            }
            yTmp[j] = y[j] + stepSize * sum;
          }

          computeDerivatives(stepStart + c[k-1] * stepSize, yTmp, yDotK[k]);

        }

        // estimate the state at the end of the step
        for (int j = 0; j < y0.length; ++j) {
          double sum    = b[0] * yDotK[0][j];
          for (int l = 1; l < stages; ++l) {
            sum    += b[l] * yDotK[l][j];
          }
          yTmp[j] = y[j] + stepSize * sum;
        }

        // estimate the error at the end of the step
        error = estimateError(yDotK, y, yTmp, stepSize);
        if (error <= 1.0) {

          // discrete events handling
          interpolator.storeTime(stepStart + stepSize);
          if (manager.evaluateStep(interpolator)) {
              final double dt = manager.getEventTime() - stepStart;
              if (Math.abs(dt) <= Math.ulp(stepStart)) {
                  // we cannot simply truncate the step, reject the current computation
                  // and let the loop compute another state with the truncated step.
                  // it is so small (much probably exactly 0 due to limited accuracy)
                  // that the code above would fail handling it.
                  // So we set up an artificial 0 size step by copying states
                  interpolator.storeTime(stepStart);
                  System.arraycopy(y, 0, yTmp, 0, y0.length);
                  hNew     = 0;
                  stepSize = 0;
                  loop     = false;
              } else {
                  // reject the step to match exactly the next switch time
                  hNew = dt;
              }
          } else {
            // accept the step
            loop = false;
          }

        } else {
          // reject the step and attempt to reduce error by stepsize control
          final double factor =
              Math.min(maxGrowth,
                       Math.max(minReduction, safety * Math.pow(error, exp)));
          hNew = filterStep(stepSize * factor, forward, false);
        }

      }

      // the step has been accepted
      final double nextStep = stepStart + stepSize;
      System.arraycopy(yTmp, 0, y, 0, y0.length);
      manager.stepAccepted(nextStep, y);
      lastStep = manager.stop();

      // provide the step data to the step handler
      interpolator.storeTime(nextStep);
      for (StepHandler handler : stepHandlers) {
          handler.handleStep(interpolator, lastStep);
      }
      stepStart = nextStep;

      if (fsal) {
        // save the last evaluation for the next step
        System.arraycopy(yDotK[stages - 1], 0, yDotK[0], 0, y0.length);
      }

      if (manager.reset(stepStart, y) && ! lastStep) {
        // some event handler has triggered changes that
        // invalidate the derivatives, we need to recompute them
        computeDerivatives(stepStart, y, yDotK[0]);
      }

      if (! lastStep) {
        // in some rare cases we may get here with stepSize = 0, for example
        // when an event occurs at integration start, reducing the first step
        // to zero; we have to reset the step to some safe non zero value
          stepSize = filterStep(stepSize, forward, true);

        // stepsize control for next step
        final double factor = Math.min(maxGrowth,
                                       Math.max(minReduction,
                                                safety * Math.pow(error, exp)));
        final double  scaledH    = stepSize * factor;
        final double  nextT      = stepStart + scaledH;
        final boolean nextIsLast = forward ? (nextT >= t) : (nextT <= t);
        hNew = filterStep(scaledH, forward, nextIsLast);
      }

    }

    final double stopTime = stepStart;
    resetInternalState();
    return stopTime;

  }
  public double integrate(final FirstOrderDifferentialEquations equations,
                          final double t0, final double[] y0,
                          final double t, final double[] y)
  throws DerivativeException, IntegratorException {

    sanityChecks(equations, t0, y0, t, y);
    setEquations(equations);
    resetEvaluations();
    final boolean forward = t > t0;

    // create some internal working arrays
    final int stages = c.length + 1;
    if (y != y0) {
      System.arraycopy(y0, 0, y, 0, y0.length);
    }
    final double[][] yDotK = new double[stages][];
    for (int i = 0; i < stages; ++i) {
      yDotK [i] = new double[y0.length];
    }
    final double[] yTmp = new double[y0.length];

    // set up an interpolator sharing the integrator arrays
    AbstractStepInterpolator interpolator;
    if (requiresDenseOutput() || (! eventsHandlersManager.isEmpty())) {
      final RungeKuttaStepInterpolator rki = (RungeKuttaStepInterpolator) prototype.copy();
      rki.reinitialize(this, yTmp, yDotK, forward);
      interpolator = rki;
    } else {
      interpolator = new DummyStepInterpolator(yTmp, yDotK[stages - 1], forward);
    }
    interpolator.storeTime(t0);

    // set up integration control objects
    stepStart = t0;
    stepSize  = forward ? step : -step;
    for (StepHandler handler : stepHandlers) {
        handler.reset();
    }
    CombinedEventsManager manager = addEndTimeChecker(t0, t, eventsHandlersManager);
    boolean lastStep = false;

    // main integration loop
    while (!lastStep) {

      interpolator.shift();

      for (boolean loop = true; loop;) {

        // first stage
        computeDerivatives(stepStart, y, yDotK[0]);

        // next stages
        for (int k = 1; k < stages; ++k) {

          for (int j = 0; j < y0.length; ++j) {
            double sum = a[k-1][0] * yDotK[0][j];
            for (int l = 1; l < k; ++l) {
              sum += a[k-1][l] * yDotK[l][j];
            }
            yTmp[j] = y[j] + stepSize * sum;
          }

          computeDerivatives(stepStart + c[k-1] * stepSize, yTmp, yDotK[k]);

        }

        // estimate the state at the end of the step
        for (int j = 0; j < y0.length; ++j) {
          double sum    = b[0] * yDotK[0][j];
          for (int l = 1; l < stages; ++l) {
            sum    += b[l] * yDotK[l][j];
          }
          yTmp[j] = y[j] + stepSize * sum;
        }

        // discrete events handling
        interpolator.storeTime(stepStart + stepSize);
        if (manager.evaluateStep(interpolator)) {
            final double dt = manager.getEventTime() - stepStart;
            if (Math.abs(dt) <= Math.ulp(stepStart)) {
                // we cannot simply truncate the step, reject the current computation
                // and let the loop compute another state with the truncated step.
                // it is so small (much probably exactly 0 due to limited accuracy)
                // that the code above would fail handling it.
                // So we set up an artificial 0 size step by copying states
                interpolator.storeTime(stepStart);
                System.arraycopy(y, 0, yTmp, 0, y0.length);
                stepSize = 0;
                loop     = false;
            } else {
                // reject the step to match exactly the next switch time
                stepSize = dt;
            }
        } else {
          loop = false;
        }

      }

      // the step has been accepted
      final double nextStep = stepStart + stepSize;
      System.arraycopy(yTmp, 0, y, 0, y0.length);
      manager.stepAccepted(nextStep, y);
      lastStep = manager.stop();

      // provide the step data to the step handler
      interpolator.storeTime(nextStep);
      for (StepHandler handler : stepHandlers) {
          handler.handleStep(interpolator, lastStep);
      }
      stepStart = nextStep;

      if (manager.reset(stepStart, y) && ! lastStep) {
        // some events handler has triggered changes that
        // invalidate the derivatives, we need to recompute them
        computeDerivatives(stepStart, y, yDotK[0]);
      }

      // make sure step size is set to default before next step
      stepSize = forward ? step : -step;

    }

    final double stopTime = stepStart;
    stepStart = Double.NaN;
    stepSize  = Double.NaN;
    return stopTime;

  }
