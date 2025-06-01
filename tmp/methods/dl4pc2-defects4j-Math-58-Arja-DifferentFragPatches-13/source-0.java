        private boolean isBetween(double value, double boundary1, double boundary2) {
            return (value >= boundary1 && value <= boundary2) ||
                   (value >= boundary2 && value <= boundary1);
        }
    public double[] fit() {
        final double[] guess = (new ParameterGuesser(getObservations())).guess();
        return fit(new Gaussian.Parametric(), guess);
    }
