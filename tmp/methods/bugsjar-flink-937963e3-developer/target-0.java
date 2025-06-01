	public ApplicationID restoreSavepoint(
			Map<JobVertexID, ExecutionJobVertex> tasks,
			String savepointPath) throws Exception {

		checkNotNull(savepointPath, "Savepoint path");

		synchronized (lock) {
			if (isShutdown()) {
				throw new IllegalStateException("CheckpointCoordinator is shut down");
			}

			long recoveryTimestamp = System.currentTimeMillis();

			LOG.info("Rolling back to savepoint '{}'.", savepointPath);

			Savepoint savepoint = savepointStore.getState(savepointPath);

			CompletedCheckpoint checkpoint = savepoint.getCompletedCheckpoint();

			LOG.info("Savepoint: {}@{}", checkpoint.getCheckpointID(), checkpoint.getTimestamp());

			// Set the initial state of all tasks
			LOG.debug("Rolling back individual operators.");
			for (StateForTask state : checkpoint.getStates()) {
				LOG.debug("Rolling back subtask {} of operator {}.",
						state.getSubtask(), state.getOperatorId());

				ExecutionJobVertex vertex = tasks.get(state.getOperatorId());

				if (vertex == null) {
					String msg = String.format("Failed to rollback to savepoint %s. " +
							"Cannot map old state for task %s to the new program. " +
							"This indicates that the program has been changed in a " +
							"non-compatible way  after the savepoint.", savepoint,
							state.getOperatorId());
					throw new IllegalStateException(msg);
				}

				if (state.getSubtask() >= vertex.getParallelism()) {
					String msg = String.format("Failed to rollback to savepoint %s. " +
							"Parallelism mismatch between savepoint state and new program. " +
							"Cannot map subtask %d of operator %s to new program with " +
							"parallelism %d. This indicates that the program has been changed " +
							"in a non-compatible way after the savepoint.", savepoint,
							state.getSubtask(), state.getOperatorId(), vertex.getParallelism());
					throw new IllegalStateException(msg);
				}

				Execution exec = vertex.getTaskVertices()[state.getSubtask()]
						.getCurrentExecutionAttempt();

				exec.setInitialState(state.getState(), recoveryTimestamp);
			}

			// Reset the checkpoint ID counter
			long nextCheckpointId = checkpoint.getCheckpointID();
			checkpointIdCounter.start();
			checkpointIdCounter.setCount(nextCheckpointId + 1);
			LOG.info("Reset the checkpoint ID to {}", nextCheckpointId);

			this.appId = savepoint.getApplicationId();
			LOG.info("Reset the application ID to {}", appId);

			return appId;
		}
	}
