    protected void start(final double t0, final double[] y0, final double t)
        throws DimensionMismatchException, NumberIsTooSmallException,
               MaxCountExceededException, NoBracketingException {

        // make sure NO user event nor user step handler is triggered,
        // this is the task of the top level integrator, not the task
        // of the starter integrator
        starter.clearEventHandlers();
        starter.clearStepHandlers();

        // set up one specific step handler to extract initial Nordsieck vector
        starter.addStepHandler(new NordsieckInitializer((nSteps + 3) / 2, y0.length));

        // start integration, expecting a InitializationCompletedMarkerException
        try {

            if (starter instanceof AbstractIntegrator) {
                ((AbstractIntegrator) starter).integrate(getExpandable(), t);
            } else {
                starter.integrate(new FirstOrderDifferentialEquations() {

                    /** {@inheritDoc} */
                    @Override
                    public int getDimension() {
                        return getExpandable().getTotalDimension();
                    }

                    /** {@inheritDoc} */
                    @Override
                    public void computeDerivatives(double t, double[] y, double[] yDot) {
                        getExpandable().computeDerivatives(t, y, yDot);
                    }

                }, t0, y0, t, new double[y0.length]);
            }

        } catch (InitializationCompletedMarkerException icme) { // NOPMD
            // this is the expected nominal interruption of the start integrator

            // count the evaluations used by the starter
            getCounter().increment(starter.getEvaluations());

        }

        // remove the specific step handler
        starter.clearStepHandlers();

    }
