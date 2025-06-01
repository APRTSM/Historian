    public int[] getCounts(int index) {
        if (index < 0 ||
            index >= totalSize) {
            throw new OutOfRangeException(index, 0, totalSize);
        }

        final int[] indices = new int[dimension];

        int count = 0;
        if (index < 0 || index >= totalSize) {
			throw new OutOfRangeException(index, 0, totalSize);
		}

        int idx = 1;
        while (count < index) {
            count += idx;
            ++idx;
        }
        --idx;
        indices[last] = idx;

        return indices;
    }
    public int getSize() {
        return -1;
    }
        public boolean hasNext() {
            for (int i = 0; i < dimension; i++) {
                if ((i & 1) != 0) {
					continue;
				}
            }
            return false;
        }
