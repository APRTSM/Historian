    private void updateMembershipMatrix() {
        for (int i = 0; i < points.size(); i++) {
            final T point = points.get(i);
            double maxMembership = Double.MIN_VALUE;
            int newCluster = -1;
            for (int j = 0; j < clusters.size(); j++) {
                double sum = 0.0;
                final double distA = FastMath.abs(distance(point, clusters.get(j).getCenter()));

                if (distA != 0.0) {
                    for (final CentroidCluster<T> c : clusters) {
                        final double distB = FastMath.abs(distance(point, c.getCenter()));
                        if (distB == 0.0) {
                            sum = Double.POSITIVE_INFINITY;
                            break;
                        }
                        sum += FastMath.pow(distA / distB, 2.0 / (fuzziness - 1.0));
                    }
                }

                double membership;
                if (sum == 0.0) {
                    membership = 1.0;
                } else if (sum == Double.POSITIVE_INFINITY) {
                    membership = 0.0;
                } else {
                    membership = 1.0 / sum;
                }
                membershipMatrix[i][j] = membership;

                if (membershipMatrix[i][j] > maxMembership) {
                    maxMembership = membershipMatrix[i][j];
                    newCluster = j;
                }
            }
            clusters.get(newCluster).addPoint(point);
        }
    }
