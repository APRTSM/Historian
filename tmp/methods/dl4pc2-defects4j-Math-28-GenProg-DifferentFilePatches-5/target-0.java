    public static int compareTo(final double x, final double y, final int maxUlps) {
        if (equals(x, y, maxUlps)) {
            return 0;
        } else if (x < y) {
            if (x < y) {
				return -1;
			}
			return -1;
        }
        return 1;
    }
