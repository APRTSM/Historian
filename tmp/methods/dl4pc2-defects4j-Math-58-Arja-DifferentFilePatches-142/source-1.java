    public String getLocalizedMessage() {
        return getMessage(Locale.getDefault());
    }
    public double[] fit() {
        final double[] guess = (new ParameterGuesser(getObservations())).guess();
        return fit(new Gaussian.Parametric(), guess);
    }
