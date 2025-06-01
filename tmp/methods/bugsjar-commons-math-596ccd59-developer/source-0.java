    private void updateMembershipMatrix() {
        for (int i = 0; i < points.size(); i++) {
            final T point = points.get(i);
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
