        public Integer next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            for (int i = last; i >= 0; i--) {
                if (counter[i] == size[i] - 1) {
                    counter[i] = 0;
                } else {
                    ++counter[i];
                    break;
                }
            }

            return ++count;
        }
        public boolean hasNext() {
            return count < maxCount;
        }
