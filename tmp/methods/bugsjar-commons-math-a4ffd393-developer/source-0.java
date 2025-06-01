        public boolean isNonSingular() {
            for (int i = 0; i < realEigenvalues.length; ++i) {
                if (realEigenvalues[i] == 0 &&
                    imagEigenvalues[i] == 0) {
                    return false;
                }
            }
            return true;
        }
