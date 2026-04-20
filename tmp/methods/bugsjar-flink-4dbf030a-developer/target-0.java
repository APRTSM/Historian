	private Buffer requestBuffer(boolean isBlocking) throws InterruptedException, IOException {
		synchronized (availableMemorySegments) {
			returnExcessMemorySegments();

			boolean askToRecycle = owner != null;

			while (availableMemorySegments.isEmpty()) {
				if (isDestroyed) {
					throw new IllegalStateException("Buffer pool is destroyed.");
				}

				if (numberOfRequestedMemorySegments < currentPoolSize) {
					final MemorySegment segment = networkBufferPool.requestMemorySegment();

					if (segment != null) {
						numberOfRequestedMemorySegments++;
						availableMemorySegments.add(segment);

						continue;
					}
				}

				if (askToRecycle) {
					owner.releaseMemory(1);
				}

				if (isBlocking) {
					availableMemorySegments.wait(2000);
				}
				else {
					return null;
				}
			}

			return new Buffer(availableMemorySegments.poll(), this);
		}
	}
