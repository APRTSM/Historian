	public void processElement(StreamRecord<IN> element) throws Exception {
		Collection<W> elementWindows = windowAssigner.assignWindows(element.getValue(),
				element.getTimestamp());

		final K key = (K) getStateBackend().getCurrentKey();

		if (windowAssigner instanceof MergingWindowAssigner) {

			MergingWindowSet<W> mergingWindows = getMergingWindowSet();

			for (W window : elementWindows) {
				// If there is a merge, it can only result in a window that contains our new
				// element because we always eagerly merge
				final Tuple1<TriggerResult> mergeTriggerResult = new Tuple1<>(TriggerResult.CONTINUE);


				// adding the new window might result in a merge, in that case the actualWindow
				// is the merged window and we work with that. If we don't merge then
				// actualWindow == window
				W actualWindow = mergingWindows.addWindow(window,
						new MergingWindowSet.MergeFunction<W>() {
							@Override
							public void merge(W mergeResult,
									Collection<W> mergedWindows, W stateWindowResult,
									Collection<W> mergedStateWindows) throws Exception {
								context.key = key;
								context.window = mergeResult;

								// store for later use
								mergeTriggerResult.f0 = context.onMerge(mergedWindows);

								for (W m : mergedWindows) {
									context.window = m;
									context.clear();
								}

								// merge the merged state windows into the newly resulting state window
								getStateBackend().mergePartitionedStates(stateWindowResult,
										mergedStateWindows,
										windowSerializer,
										(StateDescriptor<? extends MergingState<?, ?>, ?>) windowStateDescriptor);
							}
						});

				W stateWindow = mergingWindows.getStateWindow(actualWindow);
				ListState<StreamRecord<IN>> windowState = getPartitionedState(stateWindow,
						windowSerializer,
						windowStateDescriptor);
				windowState.add(element);

				context.key = key;
				context.window = actualWindow;

				// we might have already fired because of a merge but still call onElement
				// on the (possibly merged) window
				TriggerResult triggerResult = context.onElement(element);

				TriggerResult combinedTriggerResult = TriggerResult.merge(triggerResult,
						mergeTriggerResult.f0);

				processTriggerResult(combinedTriggerResult, actualWindow);
			}

		} else {
			for (W window : elementWindows) {

				ListState<StreamRecord<IN>> windowState = getPartitionedState(window,
						windowSerializer,
						windowStateDescriptor);

				windowState.add(element);

				context.key = key;
				context.window = window;
				TriggerResult triggerResult = context.onElement(element);

				processTriggerResult(triggerResult, window);
			}
		}
	}
	protected void processTriggerResult(TriggerResult triggerResult, W window) throws Exception {
		if (!triggerResult.isFire() && !triggerResult.isPurge()) {
			// do nothing
			return;
		}

		ListState<StreamRecord<IN>> windowState;

		MergingWindowSet<W> mergingWindows = null;

		if (windowAssigner instanceof MergingWindowAssigner) {
			mergingWindows = getMergingWindowSet();
			W stateWindow = mergingWindows.getStateWindow(window);
			windowState = getPartitionedState(stateWindow, windowSerializer, windowStateDescriptor);

		} else {
			windowState = getPartitionedState(window, windowSerializer, windowStateDescriptor);
		}

		if (triggerResult.isFire()) {
			timestampedCollector.setAbsoluteTimestamp(window.maxTimestamp());
			Iterable<StreamRecord<IN>> contents = windowState.get();

			// Work around type system restrictions...
			int toEvict = evictor.evict((Iterable) contents, Iterables.size(contents), context.window);

			FluentIterable<IN> projectedContents = FluentIterable
					.from(contents)
					.skip(toEvict)
					.transform(new Function<StreamRecord<IN>, IN>() {
						@Override
						public IN apply(StreamRecord<IN> input) {
							return input.getValue();
						}
					});
			userFunction.apply(context.key, context.window, projectedContents, timestampedCollector);
		}
		if (triggerResult.isPurge()) {
			windowState.clear();
			if (mergingWindows != null) {
				mergingWindows.retireWindow(window);
			}
			context.clear();
		}
	}
	public MergingWindowSet(MergingWindowAssigner<?, W> windowAssigner, ListState<Tuple2<W, W>> state) throws Exception {
		this.windowAssigner = windowAssigner;
		windows = new HashMap<>();

		for (Tuple2<W, W> window: state.get()) {
			windows.put(window.f0, window.f1);
		}
	}
	public void persist(ListState<Tuple2<W, W>> state) throws Exception {
		for (Map.Entry<W, W> window: windows.entrySet()) {
			state.add(new Tuple2<>(window.getKey(), window.getValue()));
		}
	}
	public StreamTaskState snapshotOperatorState(long checkpointId, long timestamp) throws Exception {

		if (mergingWindowsByKey != null) {
			TupleSerializer<Tuple2<W, W>> tupleSerializer = new TupleSerializer<>((Class) Tuple2.class, new TypeSerializer[] {windowSerializer, windowSerializer} );
			ListStateDescriptor<Tuple2<W, W>> mergeStateDescriptor = new ListStateDescriptor<>("merging-window-set", tupleSerializer);
			for (Map.Entry<K, MergingWindowSet<W>> key: mergingWindowsByKey.entrySet()) {
				setKeyContext(key.getKey());
				ListState<Tuple2<W, W>> mergeState = getStateBackend().getPartitionedState(null, VoidSerializer.INSTANCE, mergeStateDescriptor);
				mergeState.clear();
				key.getValue().persist(mergeState);
			}
		}

		StreamTaskState taskState = super.snapshotOperatorState(checkpointId, timestamp);

		AbstractStateBackend.CheckpointStateOutputView out =
			getStateBackend().createCheckpointStateOutputView(checkpointId, timestamp);

		out.writeInt(watermarkTimersQueue.size());
		for (Timer<K, W> timer : watermarkTimersQueue) {
			keySerializer.serialize(timer.key, out);
			windowSerializer.serialize(timer.window, out);
			out.writeLong(timer.timestamp);
		}

		out.writeInt(processingTimeTimers.size());
		for (Timer<K, W> timer : processingTimeTimersQueue) {
			keySerializer.serialize(timer.key, out);
			windowSerializer.serialize(timer.window, out);
			out.writeLong(timer.timestamp);
		}

		taskState.setOperatorState(out.closeAndGetHandle());

		return taskState;
	}
	public void processElement(StreamRecord<IN> element) throws Exception {
		Collection<W> elementWindows = windowAssigner.assignWindows(element.getValue(), element.getTimestamp());

		final K key = (K) getStateBackend().getCurrentKey();

		if (windowAssigner instanceof MergingWindowAssigner) {
			MergingWindowSet<W> mergingWindows = getMergingWindowSet();

			for (W window: elementWindows) {
				// If there is a merge, it can only result in a window that contains our new
				// element because we always eagerly merge
				final Tuple1<TriggerResult> mergeTriggerResult = new Tuple1<>(TriggerResult.CONTINUE);


				// adding the new window might result in a merge, in that case the actualWindow
				// is the merged window and we work with that. If we don't merge then
				// actualWindow == window
				W actualWindow = mergingWindows.addWindow(window, new MergingWindowSet.MergeFunction<W>() {
					@Override
					public void merge(W mergeResult,
							Collection<W> mergedWindows, W stateWindowResult,
							Collection<W> mergedStateWindows) throws Exception {
						context.key = key;
						context.window = mergeResult;

						// store for later use
						mergeTriggerResult.f0 = context.onMerge(mergedWindows);

						for (W m: mergedWindows) {
							context.window = m;
							context.clear();
						}

						// merge the merged state windows into the newly resulting state window
						getStateBackend().mergePartitionedStates(stateWindowResult,
								mergedStateWindows,
								windowSerializer,
								(StateDescriptor<? extends MergingState<?,?>, ?>) windowStateDescriptor);
					}
				});

				W stateWindow = mergingWindows.getStateWindow(actualWindow);
				AppendingState<IN, ACC> windowState = getPartitionedState(stateWindow, windowSerializer, windowStateDescriptor);
				windowState.add(element.getValue());

				context.key = key;
				context.window = actualWindow;

				// we might have already fired because of a merge but still call onElement
				// on the (possibly merged) window
				TriggerResult triggerResult = context.onElement(element);

				TriggerResult combinedTriggerResult = TriggerResult.merge(triggerResult, mergeTriggerResult.f0);

				processTriggerResult(combinedTriggerResult, actualWindow);
			}

		} else {
			for (W window: elementWindows) {

				AppendingState<IN, ACC> windowState = getPartitionedState(window, windowSerializer,
						windowStateDescriptor);

				windowState.add(element.getValue());

				context.key = key;
				context.window = window;
				TriggerResult triggerResult = context.onElement(element);

				processTriggerResult(triggerResult, window);
			}
		}
	}
	public final void close() throws Exception {
		super.close();
		timestampedCollector = null;
		watermarkTimers = null;
		watermarkTimersQueue = null;
		processingTimeTimers = null;
		processingTimeTimersQueue = null;
		context = null;
		mergingWindowsByKey = null;
	}
	private void processTriggersFor(Watermark mark) throws Exception {
		boolean fire;

		do {
			Timer<K, W> timer = watermarkTimersQueue.peek();
			if (timer != null && timer.timestamp <= mark.getTimestamp()) {
				fire = true;

				watermarkTimers.remove(timer);
				watermarkTimersQueue.remove();

				context.key = timer.key;
				context.window = timer.window;
				setKeyContext(timer.key);
				TriggerResult triggerResult = context.onEventTime(timer.timestamp);
				processTriggerResult(triggerResult, context.window);
			} else {
				fire = false;
			}
		} while (fire);
	}
	public void dispose() {
		super.dispose();
		timestampedCollector = null;
		watermarkTimers = null;
		watermarkTimersQueue = null;
		processingTimeTimers = null;
		processingTimeTimersQueue = null;
		context = null;
		mergingWindowsByKey = null;
	}
	protected void processTriggerResult(TriggerResult triggerResult, W window) throws Exception {
		if (!triggerResult.isFire() && !triggerResult.isPurge()) {
			// do nothing
			return;
		}

		AppendingState<IN, ACC> windowState;

		MergingWindowSet<W> mergingWindows = null;

		if (windowAssigner instanceof MergingWindowAssigner) {
			mergingWindows = getMergingWindowSet();
			W stateWindow = mergingWindows.getStateWindow(window);
			windowState = getPartitionedState(stateWindow, windowSerializer, windowStateDescriptor);

		} else {
			windowState = getPartitionedState(window, windowSerializer, windowStateDescriptor);
		}

		if (triggerResult.isFire()) {
			timestampedCollector.setAbsoluteTimestamp(window.maxTimestamp());
			ACC contents = windowState.get();

			userFunction.apply(context.key, context.window, contents, timestampedCollector);

		}
		if (triggerResult.isPurge()) {
			windowState.clear();
			if (mergingWindows != null) {
				mergingWindows.retireWindow(window);
			}
			context.clear();
		}
	}
	public final void trigger(long time) throws Exception {
		boolean fire;

		do {
			Timer<K, W> timer = processingTimeTimersQueue.peek();
			if (timer != null && timer.timestamp <= time) {
				fire = true;

				processingTimeTimers.remove(timer);
				processingTimeTimersQueue.remove();

				context.key = timer.key;
				context.window = timer.window;
				setKeyContext(timer.key);
				TriggerResult triggerResult = context.onProcessingTime(timer.timestamp);
				processTriggerResult(triggerResult, context.window);
			} else {
				fire = false;
			}
		} while (fire);

		// Also check any watermark timers. We might have some in here since
		// Context.registerEventTimeTimer sets a trigger if an event-time trigger is registered
		// that is already behind the watermark.
		processTriggersFor(new Watermark(currentWatermark));
	}
	protected MergingWindowSet<W> getMergingWindowSet() throws Exception {
		MergingWindowSet<W> mergingWindows = mergingWindowsByKey.get((K) getStateBackend().getCurrentKey());
		if (mergingWindows == null) {
			// try to retrieve from state

			TupleSerializer<Tuple2<W, W>> tupleSerializer = new TupleSerializer<>((Class) Tuple2.class, new TypeSerializer[] {windowSerializer, windowSerializer} );
			ListStateDescriptor<Tuple2<W, W>> mergeStateDescriptor = new ListStateDescriptor<>("merging-window-set", tupleSerializer);
			ListState<Tuple2<W, W>> mergeState = getStateBackend().getPartitionedState(null, VoidSerializer.INSTANCE, mergeStateDescriptor);

			mergingWindows = new MergingWindowSet<>((MergingWindowAssigner<? super IN, W>) windowAssigner, mergeState);
			mergeState.clear();

			mergingWindowsByKey.put((K) getStateBackend().getCurrentKey(), mergingWindows);
		}
		return mergingWindows;
	}
