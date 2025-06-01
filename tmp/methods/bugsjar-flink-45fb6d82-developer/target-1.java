	protected List<GlobalPropertiesPair> createPossibleGlobalProperties() {
		ArrayList<GlobalPropertiesPair> pairs = new ArrayList<GlobalPropertiesPair>();
		
		if (repartitionAllowed) {
			// partition both (hash or custom)
			if (this.customPartitioner == null) {

				// we accept compatible partitionings of any type
				RequestedGlobalProperties partitioned_left_any = new RequestedGlobalProperties();
				RequestedGlobalProperties partitioned_right_any = new RequestedGlobalProperties();
				partitioned_left_any.setAnyPartitioning(this.keys1);
				partitioned_right_any.setAnyPartitioning(this.keys2);
				pairs.add(new GlobalPropertiesPair(partitioned_left_any, partitioned_right_any));

				// we also explicitly add hash partitioning, as a fallback, if the any-pairs do not match
				RequestedGlobalProperties partitioned_left_hash = new RequestedGlobalProperties();
				RequestedGlobalProperties partitioned_right_hash = new RequestedGlobalProperties();
				partitioned_left_hash.setHashPartitioned(this.keys1);
				partitioned_right_hash.setHashPartitioned(this.keys2);
				pairs.add(new GlobalPropertiesPair(partitioned_left_hash, partitioned_right_hash));
			}
			else {
				RequestedGlobalProperties partitioned_left = new RequestedGlobalProperties();
				partitioned_left.setCustomPartitioned(this.keys1, this.customPartitioner);

				RequestedGlobalProperties partitioned_right = new RequestedGlobalProperties();
				partitioned_right.setCustomPartitioned(this.keys2, this.customPartitioner);

				return Collections.singletonList(new GlobalPropertiesPair(partitioned_left, partitioned_right));
			}


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
		if (this.customPartitioner == null) {
			RequestedGlobalProperties partitioned_left_any = new RequestedGlobalProperties();
			RequestedGlobalProperties partitioned_left_hash = new RequestedGlobalProperties();
			partitioned_left_any.setAnyPartitioning(this.keys1);
			partitioned_left_hash.setHashPartitioned(this.keys1);

			RequestedGlobalProperties partitioned_right_any = new RequestedGlobalProperties();
			RequestedGlobalProperties partitioned_right_hash = new RequestedGlobalProperties();
			partitioned_right_any.setAnyPartitioning(this.keys2);
			partitioned_right_hash.setHashPartitioned(this.keys2);

			return Arrays.asList(new GlobalPropertiesPair(partitioned_left_any, partitioned_right_any),
					new GlobalPropertiesPair(partitioned_left_hash, partitioned_right_hash));
		}
		else {
			RequestedGlobalProperties partitioned_left = new RequestedGlobalProperties();
			partitioned_left.setCustomPartitioned(this.keys1, this.customPartitioner);

			RequestedGlobalProperties partitioned_right = new RequestedGlobalProperties();
			partitioned_right.setCustomPartitioned(this.keys2, this.customPartitioner);

			return Collections.singletonList(new GlobalPropertiesPair(partitioned_left, partitioned_right));
		}
	}
