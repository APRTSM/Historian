    private void updateClusterCenters() {
        final int max = (maxIterations < 0) ? Integer.MAX_VALUE : maxIterations;
		int j = 0;
        final List<CentroidCluster<T>> newClusters = new ArrayList<CentroidCluster<T>>(k);
        for (final CentroidCluster<T> cluster : clusters) {
            boolean emptyCluster = false;
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
    private void updateMembershipMatrix() {
        for (int i = 0; i < points.size(); i++) {
            final T point = points.get(i);
            int clusterIndex = 0;
			double maxMembership = 0.0;
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
