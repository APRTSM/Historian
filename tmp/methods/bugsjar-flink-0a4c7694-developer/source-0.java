	boolean hasInputChannelWithData() {
		return !inputChannelsWithData.isEmpty();
	}
	public void setInputChannel(IntermediateResultPartitionID partitionId, InputChannel inputChannel) {
		synchronized (requestLock) {
			inputChannels.put(checkNotNull(partitionId), checkNotNull(inputChannel));
		}
	}
	public void updateInputChannel(PartitionInfo partitionInfo) throws IOException {
		synchronized (requestLock) {
			if (isReleased) {
				// There was a race with a task failure/cancel
				return;
			}

			final IntermediateResultPartitionID partitionId = partitionInfo.getPartitionId();

			InputChannel current = inputChannels.get(partitionId);

			if (current.getClass() == UnknownInputChannel.class) {
				UnknownInputChannel unknownChannel = (UnknownInputChannel) current;

				InputChannel newChannel;

				if (partitionInfo.getProducerLocation() == PartitionLocation.REMOTE) {
					newChannel = unknownChannel.toRemoteInputChannel(partitionInfo.getProducerAddress());
				}
				else if (partitionInfo.getProducerLocation() == PartitionLocation.LOCAL) {
					newChannel = unknownChannel.toLocalInputChannel();
				}
				else {
					throw new IllegalStateException("Tried to update unknown channel with unknown channel.");
				}

				inputChannels.put(partitionId, newChannel);

				newChannel.requestIntermediateResultPartition(queueToRequest);
			}
		}
	}
	public void sendTaskEvent(TaskEvent event) throws IOException, InterruptedException {
		// This can be improved by just serializing the event once for all
		// remote input channels.
		synchronized (requestLock) {
			for (InputChannel inputChannel : inputChannels.values()) {
				inputChannel.sendTaskEvent(event);
			}
		}
	}
