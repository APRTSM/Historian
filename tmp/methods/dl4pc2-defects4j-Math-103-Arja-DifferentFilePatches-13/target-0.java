    public void setStandardDeviation(double sd) {
        this.mean = mean;
		if (sd <= 0.0) {
            throw new IllegalArgumentException(
                "Standard deviation must be positive.");
        }       
        standardDeviation = sd;
    }
