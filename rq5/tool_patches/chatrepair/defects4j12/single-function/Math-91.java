public int compareTo(Fraction object) {
    if (this == object) {
        return 0;
    }
    long nOd = numerator * object.denominator;
    long dOn = denominator * object.numerator;
    return (nOd < dOn) ? -1 : ((nOd > dOn) ? +1 : 0);
}
