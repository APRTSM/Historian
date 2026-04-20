        public boolean isNonSingular() {
            double largestEigenvalueNorm = 0.0;
            // Looping over all values (in case they are not sorted in decreasing
            // order of their norm).
            for (int i = 0; i < realEigenvalues.length; ++i) {
                largestEigenvalueNorm = FastMath.max(largestEigenvalueNorm, eigenvalueNorm(i));
            }
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
