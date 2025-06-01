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
    public boolean evaluateStep(final StepInterpolator interpolator)
        throws DerivativeException, EventException, ConvergenceException {

        try {

            forward = interpolator.isForward();
            final double t1 = interpolator.getCurrentTime();
            final int    n  = Math.max(1, (int) Math.ceil(Math.abs(t1 - t0) / maxCheckInterval));
            final double h  = (t1 - t0) / n;

            double ta = t0;
            double ga = g0;
            double tb = t0 + (interpolator.isForward() ? convergence : -convergence);
            for (int i = 0; i < n; ++i) {

                // evaluate handler value at the end of the substep
                tb += h;
                interpolator.setInterpolatedTime(tb);
                final double gb = handler.g(tb, interpolator.getInterpolatedState());

                // check events occurrence
                if (g0Positive ^ (gb >= 0)) {
                    // there is a sign change: an event is expected during this step

                    if (ga * gb > 0) {
                        // this is a corner case:
                        // - there was an event near ta,
                        // - there is another event between ta and tb
                        // - when ta was computed, convergence was reached on the "wrong side" of the interval
                        // this implies that the real sign of ga is the same as gb, so we need to slightly
                        // shift ta to make sure ga and gb get opposite signs and the solver won't complain
                        // about bracketing
                        final double epsilon = (forward ? 0.25 : -0.25) * convergence;
                        for (int k = 0; (k < 4) && (ga * gb > 0); ++k) {
                            ta += epsilon;
                            interpolator.setInterpolatedTime(ta);
                            ga = handler.g(ta, interpolator.getInterpolatedState());
                        }
                        if (ga * gb > 0) {
                            // this should never happen
                            throw MathRuntimeException.createInternalError(null);
                        }
                    }

                    // variation direction, with respect to the integration direction
                    increasing = gb >= ga;

                    final UnivariateRealFunction f = new UnivariateRealFunction() {
                        public double value(final double t) throws FunctionEvaluationException {
                            try {
                                interpolator.setInterpolatedTime(t);
                                return handler.g(t, interpolator.getInterpolatedState());
                            } catch (DerivativeException e) {
                                throw new FunctionEvaluationException(e, t);
                            } catch (EventException e) {
                                throw new FunctionEvaluationException(e, t);
                            }
                        }
                    };
                    final BrentSolver solver = new BrentSolver();
                    solver.setAbsoluteAccuracy(convergence);
                    solver.setMaximalIterationCount(maxIterationCount);
                    final double root = (ta <= tb) ? solver.solve(f, ta, tb) : solver.solve(f, tb, ta);
                    if ((Math.abs(root - ta) <= convergence) &&
                         (Math.abs(root - previousEventTime) <= convergence)) {
                        // we have either found nothing or found (again ?) a past event, we simply ignore it
                        ta = tb;
                        ga = gb;
                    } else if (Double.isNaN(previousEventTime) ||
                               (Math.abs(previousEventTime - root) > convergence)) {
                        pendingEventTime = root;
                        if (pendingEvent && (Math.abs(t1 - pendingEventTime) <= convergence)) {
                            // we were already waiting for this event which was
                            // found during a previous call for a step that was
                            // rejected, this step must now be accepted since it
                            // properly ends exactly at the event occurrence
                            return false;
                        }
                        // either we were not waiting for the event or it has
                        // moved in such a way the step cannot be accepted
                        pendingEvent = true;
                        return true;
                    }

                } else {
                    // no sign change: there is no event for now
                    ta = tb;
                    ga = gb;
                }

            }

            // no event during the whole step
            pendingEvent     = false;
            pendingEventTime = Double.NaN;
            return false;

        } catch (FunctionEvaluationException e) {
            final Throwable cause = e.getCause();
            if ((cause != null) && (cause instanceof DerivativeException)) {
                throw (DerivativeException) cause;
            } else if ((cause != null) && (cause instanceof EventException)) {
                throw (EventException) cause;
            }
            throw new EventException(e);
        }

    }
  public void setInitialStepSize(final double initialStepSize) {
    if ((initialStepSize < minStep) || (initialStepSize > maxStep)) {
      initialStep = -1.0;
    } else {
      initialStep = initialStepSize;
    }
  }
