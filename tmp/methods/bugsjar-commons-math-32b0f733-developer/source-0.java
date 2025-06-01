    public Complex divide(Complex divisor)
        throws NullArgumentException {
        MathUtils.checkNotNull(divisor);
        if (isNaN || divisor.isNaN) {
            return NaN;
        }

        final double c = divisor.getReal();
        final double d = divisor.getImaginary();
        if (c == 0.0 && d == 0.0) {
            return NaN;
        }

        if (divisor.isInfinite() && !isInfinite()) {
            return ZERO;
        }

        if (FastMath.abs(c) < FastMath.abs(d)) {
            double q = c / d;
            double denominator = c * q + d;
            return createComplex((real * q + imaginary) / denominator,
                (imaginary * q - real) / denominator);
        } else {
            double q = d / c;
            double denominator = d * q + c;
            return createComplex((imaginary * q + real) / denominator,
                (imaginary - real * q) / denominator);
        }
    }
    public Complex(double real, double imaginary) {
        this.real = real;
        this.imaginary = imaginary;

        isNaN = Double.isNaN(real) || Double.isNaN(imaginary);
        isInfinite = !isNaN &&
            (Double.isInfinite(real) || Double.isInfinite(imaginary));
    }
    public Complex divide(double divisor) {
        if (isNaN || Double.isNaN(divisor)) {
            return NaN;
        }
        if (divisor == 0d) {
            return NaN;
        }
        if (Double.isInfinite(divisor)) {
            return !isInfinite() ? ZERO : NaN;
        }
        return createComplex(real / divisor,
                             imaginary  / divisor);
    }
