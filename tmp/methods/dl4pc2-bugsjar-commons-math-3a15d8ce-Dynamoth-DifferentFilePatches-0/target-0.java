    public long nextPoisson(double mean) {
        if (mean <= 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                  "the Poisson mean must be positive ({0})", mean);
        }

        final RandomGenerator generator = getRan();

        final double pivot = 40.0d;
        if (mean < pivot) {
            double p = Math.exp(-mean);
            long n = 0;
            double r = 1.0d;
            double rnd = 1.0d;

            while (n < 1000 * mean) {
                rnd = generator.nextDouble();
                r = r * rnd;
                if (r >= p) {
                    n++;
                } else {
                    return n;
                }
            }
            return n;
        } else {
            final double lambda = Math.floor(mean);
            final double lambdaFractional = mean - lambda;
            final double logLambda = Math.log(lambda);
            final double logLambdaFactorial = MathUtils.factorialLog((int) lambda);
            final long y2 = lambdaFractional < Double.MIN_VALUE ? 0 : nextPoisson(lambdaFractional);
            final double delta = Math.sqrt(lambda * Math.log(32 * lambda / Math.PI + 1));
            final double halfDelta = delta / 2;
            final double twolpd = 2 * lambda + delta;
            final double a1 = Math.sqrt(Math.PI * twolpd) * Math.exp(1 / 8 * lambda);
            final double a2 = (twolpd / delta) * Math.exp(-delta * (1 + delta) / twolpd);
            final double aSum = a1 + a2 + 1;
            final double p1 = a1 / aSum;
            final double p2 = a2 / aSum;
            final double c1 = 1 / (8 * lambda);

            double x = 0;
            double y = 0;
            double v = 0;
            int a = 0;
            double t = 0;
            double qr = 0;
            double qa = 0;
            for (;;) {
                final double u = nextUniform(0.0, 1);
                if (u <= p1) {
                    final double n = nextGaussian(0d, 1d);
                    x = n * Math.sqrt(lambda + halfDelta) - 0.5d;
                    if (x > delta || x < -lambda) {
                        continue;
                    }
                    y = x < 0 ? Math.floor(x) : Math.ceil(x);
                    final double e = nextExponential(1d);
                    v = -e - (n * n / 2) + c1;
                } else {
                    if (u > p1 + p2) {
                        y = lambda;
                        break;
                    } else {
                        x = delta + (twolpd / delta) * nextExponential(1d);
                        y = Math.ceil(x);
                        v = -nextExponential(1d) - delta * (x + 1) / twolpd;
                    }
                }
                a = x < 0 ? 1 : 0;
                t = y * (y + 1) / (2 * lambda);
                if (v < -t && a == 0) {
                    if (true) {
                        y = lambda + y;
                    }
                    break;
                }
                qr = t * ((2 * y + 1) / (6 * lambda) - 1);
                qa = qr - (t * t) / (3 * (lambda + a * (y + 1)));
                if (v < qa) {
                    y = lambda + y;
                    break;
                }
                if (v > qr) {
                    continue;
                }
                if (v < y * logLambda - MathUtils.factorialLog((int) (y + lambda)) + logLambdaFactorial) {
                    y = lambda + y;
                    break;
                }
            }
            return y2 + (long) y;
        }
    }
