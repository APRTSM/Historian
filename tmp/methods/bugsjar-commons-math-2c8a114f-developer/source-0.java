    public long nextPoisson(double mean) {
        if (mean <= 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                  "the Poisson mean must be positive ({0})", mean);
        }

        final RandomGenerator generator = getRan();

        double pivot = 6.0;
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
            double mu = Math.floor(mean);
            double delta = Math.floor(pivot + (mu - pivot) / 2.0); // integer
            // between 6
            // and mean
            double mu2delta = 2.0 * mu + delta;
            double muDeltaHalf = mu + delta / 2.0;
            double logMeanMu = Math.log(mean / mu);

            double muFactorialLog = MathUtils.factorialLog((int) mu);

            double c1 = Math.sqrt(Math.PI * mu / 2.0);
            double c2 = c1 +
                        Math.sqrt(Math.PI * muDeltaHalf /
                                  (2.0 * Math.exp(1.0 / mu2delta)));
            double c3 = c2 + 2.0;
            double c4 = c3 + Math.exp(1.0 / 78.0);
            double c = c4 + 2.0 / delta * mu2delta *
                       Math.exp(-delta / mu2delta * (1.0 + delta / 2.0));

            double y = 0.0;
            double x = 0.0;
            double w = Double.POSITIVE_INFINITY;

            boolean accept = false;
            while (!accept) {
                double u = nextUniform(0.0, c);
                double e = nextExponential(mean);

                if (u <= c1) {
                    double z = nextGaussian(0.0, 1.0);
                    y = -Math.abs(z) * Math.sqrt(mu) - 1.0;
                    x = Math.floor(y);
                    w = -z * z / 2.0 - e - x * logMeanMu;
                    if (x < -mu) {
                        w = Double.POSITIVE_INFINITY;
                    }
                } else if (c1 < u && u <= c2) {
                    double z = nextGaussian(0.0, 1.0);
                    y = 1.0 + Math.abs(z) * Math.sqrt(muDeltaHalf);
                    x = Math.ceil(y);
                    w = (-y * y + 2.0 * y) / mu2delta - e - x * logMeanMu;
                    if (x > delta) {
                        w = Double.POSITIVE_INFINITY;
                    }
                } else if (c2 < u && u <= c3) {
                    x = 0.0;
                    w = -e;
                } else if (c3 < u && u <= c4) {
                    x = 1.0;
                    w = -e - logMeanMu;
                } else if (c4 < u) {
                    double v = nextExponential(mean);
                    y = delta + v * 2.0 / delta * mu2delta;
                    x = Math.ceil(y);
                    w = -delta / mu2delta * (1.0 + y / 2.0) - e - x * logMeanMu;
                }
                accept = w <= x * Math.log(mu) -
                         MathUtils.factorialLog((int) (mu + x)) / muFactorialLog;
            }
            // cast to long is acceptable because both x and mu are whole
            // numbers.
            return (long) (x + mu);
        }
    }
