    private void insertionSort(final double[] work, final int begin, final int end) {
        // Arrays.sort(work, begin, end); // Would also fix MATH-1129
        for (int j = begin + 1; j < end; j++) {
            final double saved = work[j];
            int i = j - 1;
            while (i >= begin) {
                final double wi = work[i];
                if (saved < wi || Double.isNaN(wi)) {
                    work[i + 1] = wi;
                    i--;
                } else {
                    break;
                }
            }
            work[i + 1] = saved;
        }
    }
