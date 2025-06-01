	public boolean isReleased() {
		return isReleased;
	}
	abstract public boolean isReleased();
	public boolean isReleased() {
		return isReleased;
	}
	public boolean isReleased() {
		return parent.isReleased() || isReleased.get();
	}
	public Buffer getNextBuffer() throws IOException, InterruptedException {

		if (isReleased.get()) {
			return null;
		}

		// 1) In-memory
		synchronized (parent.buffers) {
			if (parent.isReleased()) {
				return null;
			}

			if (parent.spillWriter == null) {
				if (currentQueuePosition < numberOfBuffers) {
					Buffer buffer = parent.buffers.get(currentQueuePosition);

					buffer.retain();

					// TODO Fix hard coding of 8 bytes for the header
					currentBytesRead += buffer.getSize() + 8;
					currentQueuePosition++;

					return buffer;
				}

				return null;
			}
		}

		// 2) Spilled
		if (spilledView != null) {
			return spilledView.getNextBuffer();
		}

		// 3) Spilling
		// Make sure that all buffers are written before consuming them. We can't block here,
		// because this might be called from an network I/O thread.
		if (parent.spillWriter.getNumberOfOutstandingRequests() > 0) {
			return null;
		}

		if (ioMode.isSynchronous()) {
			spilledView = new SpilledSubpartitionViewSyncIO(
					parent,
					bufferProvider.getMemorySegmentSize(),
					parent.spillWriter.getChannelID(),
					currentBytesRead);
		}
		else {
			spilledView = new SpilledSubpartitionViewAsyncIO(
					parent,
					bufferProvider,
					parent.ioManager,
					parent.spillWriter.getChannelID(),
					currentBytesRead);
		}

		return spilledView.getNextBuffer();
	}
	public boolean isReleased() {
		return parent.isReleased() || isReleased;
	}
