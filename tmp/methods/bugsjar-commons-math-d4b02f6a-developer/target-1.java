    public double getResult() {
        return optima[0];
    }
    public double getFunctionValue() {
        return optimaValues[0];
    }
    public double optimize(final UnivariateRealFunction f, final GoalType goalType,
                           final double min, final double max, final double startValue)
            throws ConvergenceException, FunctionEvaluationException {
        return optimize(f, goalType, min, max);
    }
