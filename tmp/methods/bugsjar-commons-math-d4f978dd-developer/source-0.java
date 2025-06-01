    private void insertionSort(final double[] work, final int begin, final int end) {
        for (int j = begin + 1; j < end; j++) {
            final double saved = work[j];
            int i = j - 1;
            while ((i >= begin) && (saved < work[i])) {
                work[i + 1] = work[i];
                i--;
            }
            work[i + 1] = saved;
        }
    }
