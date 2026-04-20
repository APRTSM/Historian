	public BufferOrEvent getNextNonBlocked() throws IOException, InterruptedException {
		while (true) {
			// process buffered BufferOrEvents before grabbing new ones
			BufferOrEvent next;
			if (currentBuffered != null) {
				next = currentBuffered.getNext();
				if (next == null) {
					currentBuffered = queuedBuffered.pollFirst();
					if (currentBuffered != null) {
						currentBuffered.open();
					}
					return getNextNonBlocked();
				}
			}
			else {
				next = inputGate.getNextBufferOrEvent();
			}
			
			if (next != null) {
				if (isBlocked(next.getChannelIndex())) {
					// if the channel is blocked we, we just store the BufferOrEvent
					bufferSpiller.add(next);
				}
				else if (next.isBuffer() || next.getEvent().getClass() != CheckpointBarrier.class) {
					return next;
				}
				else if (!endOfStream) {
					// process barriers only if there is a chance of the checkpoint completing
					processBarrier((CheckpointBarrier) next.getEvent(), next.getChannelIndex());
				}
			}
			else if (!endOfStream) {
				// end of stream. we feed the data that is still buffered
				endOfStream = true;
				releaseBlocks();
				return getNextNonBlocked();
			}
			else {
				return null;
			}
		}
	}
