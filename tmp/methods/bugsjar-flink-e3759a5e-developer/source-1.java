	public void processWatermark(Watermark mark) throws Exception {
		while(!priorityQueue.isEmpty() && priorityQueue.peek().getTimestamp() <= mark.getTimestamp()) {
			StreamRecord<IN> streamRecord = priorityQueue.poll();

			processEvent(nfa, streamRecord.getValue(), streamRecord.getTimestamp());
		}
	}
	public void processWatermark(Watermark mark) throws Exception {
		// iterate over all keys to trigger the execution of the buffered elements
		for (KEY key: keys) {
			setKeyContext(key);

			PriorityQueue<StreamRecord<IN>> priorityQueue = getPriorityQueue();

			NFA<IN> nfa = getNFA();

			while (!priorityQueue.isEmpty() && priorityQueue.peek().getTimestamp() <= mark.getTimestamp()) {
				StreamRecord<IN> streamRecord = priorityQueue.poll();

				processEvent(nfa, streamRecord.getValue(), streamRecord.getTimestamp());
			}
		}
	}
