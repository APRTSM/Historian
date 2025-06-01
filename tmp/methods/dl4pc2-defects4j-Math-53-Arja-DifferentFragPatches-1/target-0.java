    protected Complex createComplex(double realPart, double imaginaryPart) {
        if (isNaN) {
			return NaN;
		}
		return new Complex(realPart, imaginaryPart);
    }
    public Complex add(Complex rhs)
        throws NullArgumentException {
        if (isNaN || rhs.isNaN) {
			return NaN;
		}
        return createComplex(real + rhs.getReal(),
            imaginary + rhs.getImaginary());
    }
