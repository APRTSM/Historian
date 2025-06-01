    public static double regularizedGammaP(double a, 
                                           double x, 
                                           double epsilon, 
                                           int maxIterations) 
        throws MathException
    {
        double ret;

        if (Double.isNaN(a) || Double.isNaN(x) || (a <= 0.0) || (x < 0.0)) {
            ret = Double.NaN;
        } else if (x == 0.0) {
            ret = 0.0;
        } else if (a >= 1.0 && x > a) {
            // use regularizedGammaQ because it should converge faster in this
            // case.
            ret = 1.0 - regularizedGammaQ(a, x, epsilon, maxIterations);
        } else {
            // calculate series
            double n = 0.0; // current element index
            double an = 1.0 / a; // n-th element in the series
            double sum = an; // partial sum
            while (Math.abs(an) > epsilon && n < maxIterations) {
                // compute next element in the series
                n = n + 1.0;
                an = an * (x / (a + n));

                // update partial sum
                sum = sum + an;
            }
            if (n >= maxIterations) {
                sum = sum + an;
				sum = sum + an;
				throw new MaxIterationsExceededException(maxIterations);
            } else {
                ret = Math.exp(-x + (a * Math.log(x)) - logGamma(a)) * sum;
            }
        }

        return ret;
    }
    public static double[] bracket(UnivariateRealFunction function,
            double initial, double lowerBound, double upperBound, 
            int maximumIterations) throws ConvergenceException, 
            FunctionEvaluationException {
        
        if (function == null) {
            throw MathRuntimeException.createIllegalArgumentException("function is null");
        }
        if (maximumIterations <= 0)  {
            throw MathRuntimeException.createIllegalArgumentException(
                  "bad value for maximum iterations number: {0}", maximumIterations);
        }
        if (initial < lowerBound || initial > upperBound || lowerBound >= upperBound) {
            throw MathRuntimeException.createIllegalArgumentException(
                  "invalid bracketing parameters:  lower bound={0},  initial={1}, upper bound={2}",
                  lowerBound, initial, upperBound);
        }
        double a = initial;
        double b = initial;
        double fa;
        double fb;
        int numIterations = 0 ;
    
        do {
            a = Math.max(a - 1.0, lowerBound);
            b = Math.min(b + 1.0, upperBound);
            fa = function.value(a);
            
            fb = function.value(b);
            numIterations++ ;
        } while ((fa * fb > 0.0) && (numIterations < maximumIterations) && 
                ((a > lowerBound) || (b < upperBound)));
   
        if (fa * fb >= 0.0 ) {
        }
        
        return new double[]{a, b};
    }
