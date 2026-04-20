	public void writeToOutput(final ChannelWriterOutputView output, final int start, int num) throws IOException {
		final TypeComparator<T> comparator = this.comparator;
		final TypeSerializer<T> serializer = this.serializer;
		T record = this.recordInstance;
		
		final SingleSegmentInputView inView = this.inView;
		
		final int recordsPerSegment = this.recordsPerSegment;
		int currentMemSeg = start / recordsPerSegment;
		int offset = (start % recordsPerSegment) * this.recordSize;
		
		while (num > 0) {
			final MemorySegment currentIndexSegment = this.sortBuffer.get(currentMemSeg++);
			inView.set(currentIndexSegment, offset);
			
			// check whether we have a full or partially full segment
			if (num >= recordsPerSegment && offset == 0) {
				// full segment
				for (int numInMemSeg = 0; numInMemSeg < recordsPerSegment; numInMemSeg++) {
					record = comparator.readWithKeyDenormalization(record, inView);
					serializer.serialize(record, output);
				}
				num -= recordsPerSegment;
			} else {
				// partially filled segment
				for (; num > 0 && offset <= this.lastEntryOffset; num--, offset += this.recordSize) {
					record = comparator.readWithKeyDenormalization(record, inView);
					serializer.serialize(record, output);
				}
			}

			offset = 0;
		}
	}
