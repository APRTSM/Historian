	protected void resetCurrent() {
		currentReducedMap = null;
	}
	public void evict(int n) {
		toRemove += n;
		Integer lastPreAggregateSize = elementsPerPreAggregate.peek();

		while (lastPreAggregateSize != null && lastPreAggregateSize <= toRemove) {
			toRemove = max(toRemove - elementsPerPreAggregate.removeFirst(), 0);
			reduced.removeFirst();
			lastPreAggregateSize = elementsPerPreAggregate.peek();
		}

		if (toRemove > 0 && lastPreAggregateSize == null) {
			currentReduced = null;
			toRemove = 0;
		}
	}
