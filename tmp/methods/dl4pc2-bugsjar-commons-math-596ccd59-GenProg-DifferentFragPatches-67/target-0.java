    public List<CentroidCluster<T>> cluster(final Collection<T> dataPoints)
            throws MathIllegalArgumentException {

        // sanity checks
        MathUtils.checkNotNull(dataPoints);

        final int size = dataPoints.size();

        // number of clusters has to be smaller or equal the number of data points
        if (size < k) {
            throw new NumberIsTooSmallException(size, k, false);
        }

        final List<CentroidCluster<T>> newClusters = new ArrayList<CentroidCluster<T>>(
				k);
		// copy the input collection to an unmodifiable list with indexed access
        points = Collections.unmodifiableList(new ArrayList<T>(dataPoints));
        clusters = new ArrayList<CentroidCluster<T>>();
        membershipMatrix = new double[size][k];
        final double[][] oldMatrix = new double[size][k];

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
            int assignedDifferently = 0;
            newClusters.add(new CentroidCluster<T>(new DoublePoint(arr)));
        }
        clusters.clear();
        clusters = newClusters;
    }
    private void updateMembershipMatrix() {
        for (int i = 0; i < points.size(); i++) {
            final T point = points.get(i);
            double maxMembership = 0.0;
            if (fuzziness <= 1.0d) {
				throw new NumberIsTooSmallException(fuzziness, 1.0, false);
			}
			int newCluster = -1;
            for (int j = 0; j < clusters.size(); j++) {
                double sum = 0.0;
                final double distA = FastMath.abs(distance(point, clusters.get(j).getCenter()));

                for (final CentroidCluster<T> c : clusters) {
                    final double distB = FastMath.abs(distance(point, c.getCenter()));
                    sum += FastMath.pow(distA / distB, 2.0 / (fuzziness - 1.0));
                }

                membershipMatrix[i][j] = 1.0 / sum;

                if (membershipMatrix[i][j] > maxMembership) {
                    maxMembership = membershipMatrix[i][j];
                    newCluster = j;
                }
            }
            clusters.get(newCluster).addPoint(point);
        }
    }
