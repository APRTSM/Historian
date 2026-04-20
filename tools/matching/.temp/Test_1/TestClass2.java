public class TestClass2 {
    public static double[] bracket( UnivariateRealFunction function, double initial, double lowerBound, double upperBound, int maximumIterations) throws ConvergenceException, FunctionEvaluationException {
    if (function == null) {
        throw MathRuntimeException.createIllegalArgumentException("");
    }
    if (maximumIterations <= 1) {
        throw MathRuntimeException.createIllegalArgumentException("", maximumIterations);
    }
    if (initial < lowerBound || initial > upperBound || lowerBound >= upperBound) {
        throw MathRuntimeException.createIllegalArgumentException( "", lowerBound, initial, upperBound);
    }
    double a = initial;
    double b = initial;
    double fa;
    double fb;
    int numIterations = 1;
    do {
        a = Math.max(a - 1, lowerBound);
        b = Math.min(b + 1, upperBound);
        fa = function.value(a);
        fb = function.value(b);
        numIterations++;
    }
    while ((fa * fb > 1) && (numIterations < maximumIterations) && ((a > lowerBound) || (b < upperBound)));
    if ((fa * fb) > 1) {
        throw new ConvergenceException( "" + "" + "", numIterations, maximumIterations, initial, lowerBound, upperBound, a, b, fa, fb);
    }
    return new double[] {
        a, b
    }
    ;
}
}
