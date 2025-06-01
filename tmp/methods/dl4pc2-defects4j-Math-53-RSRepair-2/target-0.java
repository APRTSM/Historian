    public Complex add(Complex rhs)
        throws NullArgumentException {
        if (isNaN || rhs.isNaN) {
			return NaN;
		}
        return createComplex(real + rhs.getReal(),
            imaginary + rhs.getImaginary());
    }
