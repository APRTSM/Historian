        public Entry next() {
		int index = next.getIndex();
		if(index < 0){
			throw new NoSuchElementException();
		}
		current.setIndex(index);
		advance(next);
		return current;
        }
        public boolean hasNext() {
            return next.getIndex() >= 0;
        }
        protected SparseEntryIterator() {
            dim = getDimension();
            current = new EntryImpl();
            next = new EntryImpl();
            if(next.getValue() == 0){
		advance(next);
            }
        }
