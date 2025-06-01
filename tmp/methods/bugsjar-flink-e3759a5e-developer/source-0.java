	public void processWatermark(Watermark mark) throws Exception {
		while(!priorityQueue.isEmpty() && priorityQueue.peek().getTimestamp() <= mark.getTimestamp()) {
			StreamRecord<IN> streamRecord = priorityQueue.poll();

			processEvent(nfa, streamRecord.getValue(), streamRecord.getTimestamp());
		}
	}
