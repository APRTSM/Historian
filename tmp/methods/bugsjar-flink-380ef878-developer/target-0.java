	private boolean decodeBufferOrEvent(RemoteInputChannel inputChannel, NettyMessage.BufferResponse bufferOrEvent) throws Throwable {
		boolean releaseNettyBuffer = true;

		try {
			if (bufferOrEvent.isBuffer()) {
				// ---- Buffer ------------------------------------------------

				// Early return for empty buffers. Otherwise Netty's readBytes() throws an
				// IndexOutOfBoundsException.
				if (bufferOrEvent.getSize() == 0) {
					return true;
				}

				BufferProvider bufferProvider = inputChannel.getBufferProvider();

				if (bufferProvider == null) {
					return false; // receiver has been cancelled/failed
				}

				while (true) {
					Buffer buffer = bufferProvider.requestBuffer();

					if (buffer != null) {
						buffer.setSize(bufferOrEvent.getSize());
						bufferOrEvent.getNettyBuffer().readBytes(buffer.getNioBuffer());

						inputChannel.onBuffer(buffer, bufferOrEvent.sequenceNumber);

						return true;
					}
					else if (bufferListener.waitForBuffer(bufferProvider, bufferOrEvent)) {
						releaseNettyBuffer = false;

						return false;
					}
					else if (bufferProvider.isDestroyed()) {
						return false;
					}
				}
			}
			else {
				// ---- Event -------------------------------------------------
				// TODO We can just keep the serialized data in the Netty buffer and release it later at the reader
				byte[] byteArray = new byte[bufferOrEvent.getSize()];
				bufferOrEvent.getNettyBuffer().readBytes(byteArray);

				Buffer buffer = new Buffer(new MemorySegment(byteArray), EventSerializer.RECYCLER, false);

				inputChannel.onBuffer(buffer, bufferOrEvent.sequenceNumber);

				return true;
			}
		}
		finally {
			if (releaseNettyBuffer) {
				bufferOrEvent.releaseBuffer();
			}
		}
	}
	private void notifyAllChannelsOfErrorAndClose(Throwable cause) {
		if (channelError.compareAndSet(false, true)) {
			for (RemoteInputChannel inputChannel : inputChannels.values()) {
				inputChannel.onError(cause);
			}

			inputChannels.clear();

			if (ctx != null) {
				ctx.close();
			}
		}
	}
