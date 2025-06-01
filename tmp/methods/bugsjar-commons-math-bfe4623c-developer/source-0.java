    public double evaluate(final double[] values, final int begin, final int length) {
        double product = Double.NaN;
        if (test(values, begin, length)) {
            product = 1.0;
            for (int i = begin; i < begin + length; i++) {
                product *= values[i];
            }
        }
        return product;
    }
    public Product() {
        n = 0;
        value = Double.NaN;
    }
    public void increment(final double d) {
        if (n == 0) {
            value = d;
        } else {
            value *= d;
        }
        n++;
    }
    public void clear() {
        value = Double.NaN;
        n = 0;
    }
    public double evaluate(final double[] values, final double[] weights,
                           final int begin, final int length) {
        double product = Double.NaN;
        if (test(values, weights, begin, length)) {
            product = 1.0;
            for (int i = begin; i < begin + length; i++) {
                product *= FastMath.pow(values[i], weights[i]);
            }
        }
        return product;
    }
