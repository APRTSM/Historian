    public Complex add(Complex rhs)
        throws NullArgumentException {
        if (isNaN || rhs.isNaN) {
				return NaN;
			}
		MathUtils.checkNotNull(rhs);
        return createComplex(real + rhs.getReal(),
            imaginary + rhs.getImaginary());
    }
    protected Complex createComplex(double realPart, double imaginaryPart) {
        if (isNaN) {
			return NaN;
		}
		return new Complex(realPart, imaginaryPart);
    }
