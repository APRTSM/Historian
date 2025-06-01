    public boolean evaluateStep(final StepInterpolator interpolator)
    throws DerivativeException, IntegratorException {

        try {

            first = null;
            if (states.isEmpty()) {
                // there is nothing to do, return now to avoid setting the
                // interpolator time (and hence avoid unneeded calls to the
                // user function due to interpolator finalization)
                return false;
            }

            if (! initialized) {

                // initialize the events states
                final double t0 = interpolator.getPreviousTime();
                interpolator.setInterpolatedTime(t0);
                final double [] y = interpolator.getInterpolatedState();
                for (EventState state : states) {
                    state.reinitializeBegin(t0, y);
                }

                initialized = true;

            }

            // check events occurrence
            for (EventState state : states) {

                if (state.evaluateStep(interpolator)) {
                    if (first == null) {
                        first = state;
                    } else {
                        if (interpolator.isForward()) {
                            if (state.getEventTime() < first.getEventTime()) {
                                first = state;
                            }
                        } else {
                            if (state.getEventTime() > first.getEventTime()) {
                                first = state;
                            }
                        }
                    }
                }

            }

            return first != null;

        } catch (EventException se) {
            throw new IntegratorException(se);
        } catch (ConvergenceException ce) {
            throw new IntegratorException(ce);
        }

    }
  public double initializeStep(final FirstOrderDifferentialEquations equations,
                               final boolean forward, final int order, final double[] scale,
                               final double t0, final double[] y0, final double[] yDot0,
                               final double[] y1, final double[] yDot1)
      throws DerivativeException {

    if (initialStep > 0) {
      // use the user provided value
      return forward ? initialStep : -initialStep;
    }

    // very rough first guess : h = 0.01 * ||y/scale|| / ||y'/scale||
    // this guess will be used to perform an Euler step
    double ratio;
    double yOnScale2 = 0;
    double yDotOnScale2 = 0;
    for (int j = 0; j < y0.length; ++j) {
      ratio         = y0[j] / scale[j];
      yOnScale2    += ratio * ratio;
      ratio         = yDot0[j] / scale[j];
      yDotOnScale2 += ratio * ratio;
    }

    double h = ((yOnScale2 < 1.0e-10) || (yDotOnScale2 < 1.0e-10)) ?
               1.0e-6 : (0.01 * Math.sqrt(yOnScale2 / yDotOnScale2));
    if (! forward) {
      h = -h;
    }

    // perform an Euler step using the preceding rough guess
    for (int j = 0; j < y0.length; ++j) {
      y1[j] = y0[j] + h * yDot0[j];
    }
    computeDerivatives(t0 + h, y1, yDot1);

    // estimate the second derivative of the solution
    double yDDotOnScale = 0;
    for (int j = 0; j < y0.length; ++j) {
      ratio         = (yDot1[j] - yDot0[j]) / scale[j];
      yDDotOnScale += ratio * ratio;
    }
    yDDotOnScale = Math.sqrt(yDDotOnScale) / h;

    // step size is computed such that
    // h^order * max (||y'/tol||, ||y''/tol||) = 0.01
    final double maxInv2 = Math.max(Math.sqrt(yDotOnScale2), yDDotOnScale);
    final double h1 = (maxInv2 < 1.0e-15) ?
                      Math.max(1.0e-6, 0.001 * Math.abs(h)) :
                      Math.pow(0.01 / maxInv2, 1.0 / order);
    h = Math.min(100.0 * Math.abs(h), h1);
    h = Math.max(h, 1.0e-12 * Math.abs(t0));  // avoids cancellation when computing t1 - t0
    if (h < getMinStep()) {
      h = getMinStep();
    }
    if (h > getMaxStep()) {
      h = getMaxStep();
    }
    if (! forward) {
      h = -h;
    }

    return h;

  }
