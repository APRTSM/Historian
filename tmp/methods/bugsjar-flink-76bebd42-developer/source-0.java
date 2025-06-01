		int getBlockCount() {
			return this.currentBlockNumber + 1;
		}
	public int getNumOccupiedMemorySegments() {
		// either the number of memory segments, or one for spilling
		final int numPartitionBuffers = this.partitionBuffers != null ? this.partitionBuffers.length : 1;
		return numPartitionBuffers + numOverflowSegments;
	}
