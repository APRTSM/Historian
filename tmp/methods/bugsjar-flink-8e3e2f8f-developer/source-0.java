	public void onCompletedCheckpoint(CompletedCheckpoint checkpoint) {
		// Sanity check
		if (taskParallelism.isEmpty()) {
			return;
		}

		synchronized (statsLock) {
			int overallStateSize = 0;

			// Operator stats
			Map<JobVertexID, long[][]> statsForSubTasks = new HashMap<>();

			for (StateForTask state : checkpoint.getStates()) {
				// Job-level checkpoint size is sum of all state sizes
				overallStateSize += state.getStateSize();

				// Subtask stats
				JobVertexID opId = state.getOperatorId();
				long[][] statsPerSubtask = statsForSubTasks.get(opId);

				if (statsPerSubtask == null) {
					int parallelism = taskParallelism.get(opId);
					statsPerSubtask = new long[parallelism][2];
					statsForSubTasks.put(opId, statsPerSubtask);
				}

				int subTaskIndex = state.getSubtask();
				if (subTaskIndex < statsPerSubtask.length) {
					statsPerSubtask[subTaskIndex][0] = state.getDuration();
					statsPerSubtask[subTaskIndex][1] = state.getStateSize();
				}
			}

			// It is possible that completed checkpoints are added out of
			// order. Make sure that in this case the last completed
			// checkpoint is not updated.
			boolean isInOrder = latestCompletedCheckpoint != null &&
					checkpoint.getCheckpointID() > latestCompletedCheckpoint.getCheckpointID();

			// Clear this in each case
			lastJobStats = null;

			if (overallCount == 0 || isInOrder) {
				latestCompletedCheckpoint = checkpoint;

				// Clear cached stats
				operatorStatsCache.clear();

				// Update the stats per sub task
				subTaskStats = statsForSubTasks;
			}

			long checkpointId = checkpoint.getCheckpointID();
			long checkpointTriggerTimestamp = checkpoint.getTimestamp();
			long checkpointDuration = checkpoint.getDuration();

			overallCount++;

			// Duration stats
			if (checkpointDuration > overallMaxDuration) {
				overallMaxDuration = checkpointDuration;
			}

			if (checkpointDuration < overallMinDuration) {
				overallMinDuration = checkpointDuration;
			}

			overallTotalDuration += checkpointDuration;

			// State size stats
			if (overallStateSize < overallMinStateSize) {
				overallMinStateSize = overallStateSize;
			}

			if (overallStateSize > overallMaxStateSize) {
				overallMaxStateSize = overallStateSize;
			}

			this.overallTotalStateSize += overallStateSize;

			// Recent history
			if (historySize > 0) {
				CheckpointStats stats = new CheckpointStats(
						checkpointId,
						checkpointTriggerTimestamp,
						checkpointDuration,
						overallStateSize);

				if (isInOrder) {
					if (history.size() == historySize) {
						history.remove(0);
					}

					history.add(stats);
				}
				else {
					final int size = history.size();

					// Only remove it if it the new checkpoint is not too old
					if (size == historySize) {
						if (checkpointId > history.get(0).getCheckpointId()) {
							history.remove(0);
						}
					}

					int pos = 0;

					// Find position
					for (int i = 0; i < size; i++) {
						pos = i;

						if (checkpointId < history.get(i).getCheckpointId()) {
							break;
						}
					}

					history.add(pos, stats);
				}
			}
		}
	}
