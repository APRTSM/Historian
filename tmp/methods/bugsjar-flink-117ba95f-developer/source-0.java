	public Option<OperatorCheckpointStats> getOperatorStats(JobVertexID operatorId) {
		synchronized (statsLock) {
			OperatorCheckpointStats stats = operatorStatsCache.get(operatorId);

			if (stats != null) {
				return Option.apply(stats);
			}
			else if (latestCompletedCheckpoint != null && subTaskStats != null) {
				long[][] subTaskStats = this.subTaskStats.get(operatorId);

				if (subTaskStats == null) {
					throw new IllegalArgumentException("Unknown operator ID.");
				}

				long maxDuration = Long.MIN_VALUE;
				long stateSize = 0;

				for (long[] subTaskStat : subTaskStats) {
					if (subTaskStat[0] > maxDuration) {
						maxDuration = subTaskStat[0];
					}

					stateSize += subTaskStat[1];
				}

				stats = new OperatorCheckpointStats(
						latestCompletedCheckpoint.getCheckpointID(),
						latestCompletedCheckpoint.getTimestamp(),
						maxDuration,
						stateSize,
						subTaskStats);

				// Remember this and don't recompute if requested again
				operatorStatsCache.put(operatorId, stats);

				return Option.apply(stats);
			}
			else {
				return Option.empty();
			}
		}
	}
