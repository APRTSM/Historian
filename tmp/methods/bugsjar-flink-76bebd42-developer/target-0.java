	public int getNumOccupiedMemorySegments() {
		// either the number of memory segments, or one for spilling
		final int numPartitionBuffers = this.partitionBuffers != null ?
			this.partitionBuffers.length : this.buildSideWriteBuffer.getNumOccupiedMemorySegments();
		return numPartitionBuffers + numOverflowSegments;
	}
		int getNumOccupiedMemorySegments() {
			// return the current segment + all filled segments
			return this.targetList.size() + 1;
		}
