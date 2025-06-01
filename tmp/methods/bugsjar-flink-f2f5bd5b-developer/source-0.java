	public void processElement(StreamRecord<IN> element) throws Exception {
		Collection<W> elementWindows = windowAssigner.assignWindows(element.getValue(),
				element.getTimestamp());

		K key = (K) getStateBackend().getCurrentKey();

		if (windowAssigner instanceof MergingWindowAssigner) {
			MergingWindowSet<W> mergingWindows = mergingWindowsByKey.get(getStateBackend().getCurrentKey());
			if (mergingWindows == null) {
				mergingWindows = new MergingWindowSet<>((MergingWindowAssigner<? super IN, W>) windowAssigner);
				mergingWindowsByKey.put(key, mergingWindows);
			}


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

				processTriggerResult(combinedTriggerResult, key, actualWindow);
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

				processTriggerResult(triggerResult, key, window);
			}
		}
	}
	protected void processTriggerResult(TriggerResult triggerResult, K key, W window) throws Exception {
		if (!triggerResult.isFire() && !triggerResult.isPurge()) {
			// do nothing
			return;
		}

		ListState<StreamRecord<IN>> windowState;

		MergingWindowSet<W> mergingWindows = null;

		if (windowAssigner instanceof MergingWindowAssigner) {
			mergingWindows = mergingWindowsByKey.get(key);
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
