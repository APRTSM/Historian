    public double evaluate(final double[] values, final int begin, final int length) {
        double product = Double.NaN;
        if (test(values, begin, length, true)) {
            product = 1.0;
            for (int i = begin; i < begin + length; i++) {
                product *= values[i];
            }
        }
        return product;
    }
    public void clear() {
        value = 1;
        n = 0;
    }
    public Product() {
        n = 0;
        value = 1;
    }
    public double evaluate(final double[] values, final double[] weights,
                           final int begin, final int length) {
        double product = Double.NaN;
        if (test(values, weights, begin, length, true)) {
            product = 1.0;
            for (int i = begin; i < begin + length; i++) {
                product *= FastMath.pow(values[i], weights[i]);
            }
        }
        return product;
    }
    public void increment(final double d) {
        value *= d;
        n++;
    }
