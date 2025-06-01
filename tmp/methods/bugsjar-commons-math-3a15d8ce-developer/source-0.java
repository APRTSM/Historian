        public Entry next() {
            tmp.setIndex(current.getIndex());
            if (next != null) {
                current.setIndex(next.getIndex());
                advance(next);
                if (next.getIndex() < 0) {
                    next = null;
                }
            } else {
                current = null;
            }
            return tmp;
        }
        public boolean hasNext() {
            return current != null;
        }
        protected SparseEntryIterator() {
            dim = getDimension();
            current = new EntryImpl();
            if (current.getValue() == 0) {
                advance(current);
            }
            if(current.getIndex() >= 0){
                // There is at least one non-zero entry
                next = new EntryImpl();
                next.setIndex(current.getIndex());
                advance(next);
            } else {
                // The vector consists of only zero entries, so deny having a next
                current = null;
            }
        }
