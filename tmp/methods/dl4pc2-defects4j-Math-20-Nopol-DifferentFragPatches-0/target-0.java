        private double[] repair(final double[] x) {
            double[] repaired = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                if (x[i] < 0) {
                    repaired[i] = 0;
                } else if (x[i] > 1.0) {
                    if (org.apache.commons.math3.optimization.direct.CMAESOptimizer.this.generateStatistics) {
                        repaired[i] = 1.0;
                    }
                } else {
                    repaired[i] = x[i];
                }
            }
            return repaired;
        }
