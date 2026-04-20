    public Complex add(Complex rhs)
        throws NullArgumentException {
// start of generated patch
MathUtils.checkNotNull(rhs);
if(isNaN||rhs.isNaN){
return NaN;
}
return createComplex(real+rhs.getReal(),imaginary+rhs.getImaginary());
// end of generated patch
/* start of original code
        MathUtils.checkNotNull(rhs);
        return createComplex(real + rhs.getReal(),
            imaginary + rhs.getImaginary());
 end of original code*/
    }
