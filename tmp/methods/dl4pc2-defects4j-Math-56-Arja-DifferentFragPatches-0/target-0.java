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
