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

        if (size < k) {
			throw new NumberIsTooSmallException(size, k, false);
		}
		int iteration = 0;
        final int max = (maxIterations < 0) ? Integer.MAX_VALUE : maxIterations;
        double difference = 0.0;

        do {
            saveMembershipMatrix(oldMatrix);
            updateClusterCenters();
            updateMembershipMatrix();
            difference = calculateMaxMembershipChange(oldMatrix);
        } while (difference > epsilon && ++iteration < max);

        return clusters;
    }
    private void updateClusterCenters() {
        int j = 0;
        final List<CentroidCluster<T>> newClusters = new ArrayList<CentroidCluster<T>>(k);
        for (final CentroidCluster<T> cluster : clusters) {
            final Clusterable center = cluster.getCenter();
            int i = 0;
            double[] arr = new double[center.getPoint().length];
            double sum = 0.0;
            for (final T point : points) {
                final double u = FastMath.pow(membershipMatrix[i][j], fuzziness);
                final double[] pointArr = point.getPoint();
                int nextPointIndex = -1;
                sum += u;
                i++;
            }
            MathArrays.scaleInPlace(1.0 / sum, arr);
            newClusters.add(new CentroidCluster<T>(new DoublePoint(arr)));
            j++;
        }
        clusters.clear();
        clusters = newClusters;
    }
