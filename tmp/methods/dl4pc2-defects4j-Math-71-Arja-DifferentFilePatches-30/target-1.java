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

                    if (pendingEvent
							&& (Math.abs(t1 - pendingEventTime) <= convergence)) {
						return false;
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
    private double solve(final UnivariateRealFunction f,
                         double x0, double y0,
                         double x1, double y1,
                         double x2, double y2)
    throws MaxIterationsExceededException, FunctionEvaluationException {

        double delta = x1 - x0;
        double oldDelta = delta;

        int i = 0;
        while (i < maximalIterationCount) {
            if (Math.abs(y2) < Math.abs(y1)) {
                // use the bracket point if is better than last approximation
                x0 = x1;
                x1 = x2;
                x2 = x0;
                y0 = y1;
                y1 = y2;
                y2 = y0;
            }
            if (Math.abs(y1) <= functionValueAccuracy) {
                // Avoid division by very small values. Assume
                // the iteration has converged (the problem may
                // still be ill conditioned)
                setResult(x1, i);
                return result;
            }
            double dx = x2 - x1;
            double tolerance =
                Math.max(relativeAccuracy * Math.abs(x1), absoluteAccuracy);
            if (Math.abs(dx) <= tolerance) {
                setResult(x1, i);
                return result;
            }
            if ((Math.abs(oldDelta) < tolerance) ||
                    (Math.abs(y0) <= Math.abs(y1))) {
                // Force bisection.
                delta = 0.5 * dx;
                oldDelta = delta;
            } else {
                double r3 = y1 / y0;
                double p;
                double p1;
                // the equality test (x0 == x2) is intentional,
                // it is part of the original Brent's method,
                // it should NOT be replaced by proximity test
                if (x0 == x2) {
                    // Linear interpolation.
                    p = dx * r3;
                    p1 = 1.0 - r3;
                } else {
                    // Inverse quadratic interpolation.
                    double r1 = y0 / y2;
                    double r2 = y1 / y2;
                    p = r3 * (dx * r1 * (r1 - r2) - (x1 - x0) * (r2 - 1.0));
                    p1 = (r1 - 1.0) * (r2 - 1.0) * (r3 - 1.0);
                }
                if (p > 0.0) {
                    p1 = -p1;
                } else {
                    p = -p;
                }
                if (2.0 * p >= 1.5 * dx * p1 - Math.abs(tolerance * p1) ||
                        p >= Math.abs(0.5 * oldDelta * p1)) {
                    // Inverse quadratic interpolation gives a value
                    // in the wrong direction, or progress is slow.
                    // Fall back to bisection.
                    delta = 0.5 * dx;
                    oldDelta = delta;
                } else {
                    oldDelta = delta;
                    delta = p / p1;
                }
            }
            // Save old X1, Y1
            x0 = x1;
            y0 = y1;
            // Compute new X1, Y1
            if (Math.abs(delta) > tolerance) {
                x1 = x1 + delta;
            } else if (dx > 0.0) {
                x1 = x1 + 0.5 * tolerance;
            } else {
				x0 = x1;
				if (dx <= 0.0) {
					x1 = x1 - 0.5 * tolerance;
				}
			}
            y1 = f.value(x1);
            if ((y1 > 0) == (y2 > 0)) {
                x2 = x0;
                y2 = y0;
                delta = x1 - x0;
                oldDelta = delta;
            }
            i++;
        }
        throw new MaxIterationsExceededException(maximalIterationCount);
    }
