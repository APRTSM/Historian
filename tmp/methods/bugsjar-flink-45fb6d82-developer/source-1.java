	protected List<GlobalPropertiesPair> createPossibleGlobalProperties() {
		ArrayList<GlobalPropertiesPair> pairs = new ArrayList<GlobalPropertiesPair>();
		
		if (repartitionAllowed) {
			// partition both (hash or custom)
			RequestedGlobalProperties partitioned1 = new RequestedGlobalProperties();
			if (customPartitioner == null) {
				partitioned1.setAnyPartitioning(this.keys1);
			} else {
				partitioned1.setCustomPartitioned(this.keys1, this.customPartitioner);
			}
			
			RequestedGlobalProperties partitioned2 = new RequestedGlobalProperties();
			if (customPartitioner == null) {
				partitioned2.setAnyPartitioning(this.keys2);
			} else {
				partitioned2.setCustomPartitioned(this.keys2, this.customPartitioner);
			}
			
			pairs.add(new GlobalPropertiesPair(partitioned1, partitioned2));
		}
		
		if (broadcastSecondAllowed) {
			// replicate second
			RequestedGlobalProperties any1 = new RequestedGlobalProperties();
			RequestedGlobalProperties replicated2 = new RequestedGlobalProperties();
			replicated2.setFullyReplicated();
			pairs.add(new GlobalPropertiesPair(any1, replicated2));
		}
		
		if (broadcastFirstAllowed) {
			// replicate first
			RequestedGlobalProperties replicated1 = new RequestedGlobalProperties();
			replicated1.setFullyReplicated();
			RequestedGlobalProperties any2 = new RequestedGlobalProperties();
			pairs.add(new GlobalPropertiesPair(replicated1, any2));
		}
		return pairs;
	}
	protected List<GlobalPropertiesPair> createPossibleGlobalProperties() {
		RequestedGlobalProperties partitioned1 = new RequestedGlobalProperties();
		if (this.customPartitioner == null) {
			partitioned1.setAnyPartitioning(this.keys1);
		} else {
			partitioned1.setCustomPartitioned(this.keys1, this.customPartitioner);
		}
		
		RequestedGlobalProperties partitioned2 = new RequestedGlobalProperties();
		if (this.customPartitioner == null) {
			partitioned2.setAnyPartitioning(this.keys2);
		} else {
			partitioned2.setCustomPartitioned(this.keys2, this.customPartitioner);
		}
		
		return Collections.singletonList(new GlobalPropertiesPair(partitioned1, partitioned2));
	}
