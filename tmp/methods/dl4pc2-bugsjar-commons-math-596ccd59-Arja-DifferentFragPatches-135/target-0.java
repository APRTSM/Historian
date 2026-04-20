    public List<CentroidCluster<T>> cluster(final Collection<T> dataPoints)
            throws MathIllegalArgumentException {

        // sanity checks
        MathUtils.checkNotNull(dataPoints);

        final int size = dataPoints.size();

        // number of clusters has to be smaller or equal the number of data points
        if (size < k) {
            throw new NumberIsTooSmallException(size, k, false);
        }

        // copy the input collection to an unmodifiable list with indexed access
        points = Collections.unmodifiableList(new ArrayList<T>(dataPoints));
        clusters = new ArrayList<CentroidCluster<T>>();
        membershipMatrix = new double[size][k];
        final double[][] oldMatrix = new double[size][k];

        // if no points are provided, return an empty list of clusters
        if (size == 0) {
            return clusters;
        }

        initializeMembershipMatrix();

        // there is at least one point
        final int pointDimension = points.get(0).getPoint().length;
        for (int i = 0; i < k; i++) {
            clusters.add(new CentroidCluster<T>(new DoublePoint(new double[pointDimension])));
        }

        int iteration = 0;
        final int max = (maxIterations < 0) ? Integer.MAX_VALUE : maxIterations;
        double difference = 0.0;

        do {
            saveMembershipMatrix(oldMatrix);
            difference = calculateMaxMembershipChange(oldMatrix);
            updateMembershipMatrix();
            difference = calculateMaxMembershipChange(oldMatrix);
        } while (difference > epsilon && ++iteration < max);

        return clusters;
    }
