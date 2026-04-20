        public boolean isNonSingular() {
            // The eigenvalues are sorted by size, descending
            double largestEigenvalueNorm = eigenvalueNorm(0);
            // Corner case: zero matrix, all exactly 0 eigenvalues
            if (largestEigenvalueNorm == 0.0) {
                return false;
            }
            for (int i = 0; i < realEigenvalues.length; ++i) {
                // Looking for eigenvalues that are 0, where we consider anything much much smaller
                // than the largest eigenvalue to be effectively 0.
                if (Precision.equals(eigenvalueNorm(i) / largestEigenvalueNorm, 0, EPSILON)) {
                    return false;
                }
            }
            return true;
        }
        private double eigenvalueNorm(int i) {
            final double re = realEigenvalues[i];
            final double im = imagEigenvalues[i];
            return FastMath.sqrt(re * re + im * im);
        }
