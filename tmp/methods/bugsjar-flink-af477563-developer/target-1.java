	public int getNumOccupiedMemorySegments() {
		// either the number of memory segments, or one for spilling
		final int numPartitionBuffers = this.partitionBuffers != null ? this.partitionBuffers.length : 1;
		return numPartitionBuffers + numOverflowSegments;
	}
	public int spillPartition(List<MemorySegment> target, IOManager ioAccess, FileIOChannel.ID targetChannel,
			LinkedBlockingQueue<MemorySegment> bufferReturnQueue)
	throws IOException
	{
		// sanity checks
		if (!isInMemory()) {
			throw new RuntimeException("Bug in Hybrid Hash Join: " +
					"Request to spill a partition that has already been spilled.");
		}
		if (getNumOccupiedMemorySegments() < 2) {
			throw new RuntimeException("Bug in Hybrid Hash Join: " +
				"Request to spill a partition with less than two buffers.");
		}
		
		// return the memory from the overflow segments
		for (int i = 0; i < this.numOverflowSegments; i++) {
			target.add(this.overflowSegments[i]);
		}
		this.overflowSegments = null;
		this.numOverflowSegments = 0;
		this.nextOverflowBucket = 0;
		
		// create the channel block writer and spill the current buffers
		// that keep the build side buffers current block, as it is most likely not full, yet
		// we return the number of blocks that become available
		this.buildSideChannel = ioAccess.createBlockChannelWriter(targetChannel, bufferReturnQueue);
		return this.buildSideWriteBuffer.spill(this.buildSideChannel);
	}
	protected int spillPartition() throws IOException {
		// find the largest partition
		ArrayList<HashPartition<BT, PT>> partitions = this.partitionsBeingBuilt;
		int largestNumBlocks = 0;
		int largestPartNum = -1;
		
		for (int i = 0; i < partitions.size(); i++) {
			HashPartition<BT, PT> p = partitions.get(i);
			if (p.isInMemory() && p.getNumOccupiedMemorySegments() > largestNumBlocks) {
				largestNumBlocks = p.getNumOccupiedMemorySegments();
				largestPartNum = i;
			}
		}
		final HashPartition<BT, PT> p = partitions.get(largestPartNum);
		
		if (useBloomFilters) {
			buildBloomFilterForBucketsInPartition(largestPartNum, p);
		}
		
		// spill the partition
		int numBuffersFreed = p.spillPartition(this.availableMemory, this.ioManager, 
										this.currentEnumerator.next(), this.writeBehindBuffers);
		this.writeBehindBuffersAvailable += numBuffersFreed;
		// grab as many buffers as are available directly
		MemorySegment currBuff;
		while (this.writeBehindBuffersAvailable > 0 && (currBuff = this.writeBehindBuffers.poll()) != null) {
			this.availableMemory.add(currBuff);
			this.writeBehindBuffersAvailable--;
		}
		return largestPartNum;
	}
