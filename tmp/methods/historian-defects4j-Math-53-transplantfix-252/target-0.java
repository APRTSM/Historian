    public Complex add(Complex rhs)
        throws NullArgumentException {
        MathUtils.checkNotNull(rhs);
        if (isNaN || rhs.isNaN) {
return NaN;
}
else {
double imaginary = rhs.getReal();
}

return createComplex(real + rhs.getReal(),
            imaginary + rhs.getImaginary());
    }
