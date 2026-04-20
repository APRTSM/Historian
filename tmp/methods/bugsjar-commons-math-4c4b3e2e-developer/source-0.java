    public Fraction divide(final int i) {
        return new Fraction(numerator, denominator * i);
    }
    public Fraction multiply(final int i) {
        return new Fraction(numerator * i, denominator);
    }
