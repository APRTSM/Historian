	public void restart() {
		try {
			synchronized (progressLock) {
				JobStatus current = state;

				if (current == JobStatus.CANCELED) {
					LOG.info("Canceled job during restart. Aborting restart.");
					return;
				}
				else if (current != JobStatus.RESTARTING) {
					throw new IllegalStateException("Can only restart job from state restarting.");
				}

				if (scheduler == null) {
					throw new IllegalStateException("The execution graph has not been scheduled before - scheduler is null.");
				}

				this.currentExecutions.clear();

				for (ExecutionJobVertex jv : this.verticesInCreationOrder) {
					jv.resetForNewExecution();
				}

				for (int i = 0; i < stateTimestamps.length; i++) {
					stateTimestamps[i] = 0;
				}
				numFinishedJobVertices = 0;
				transitionState(JobStatus.RESTARTING, JobStatus.CREATED);
				
				// if we have checkpointed state, reload it into the executions
				if (checkpointCoordinator != null) {
					checkpointCoordinator.restoreLatestCheckpointedState(getAllVertices(), false, false);
				}
			}

			scheduleForExecution(scheduler);
		}
		catch (Throwable t) {
			fail(t);
		}
	}
	public void cancel() {
		while (true) {
			JobStatus current = state;
			
			if (current == JobStatus.RUNNING || current == JobStatus.CREATED) {
				if (transitionState(current, JobStatus.CANCELLING)) {
					for (ExecutionJobVertex ejv : verticesInCreationOrder) {
						ejv.cancel();
					}
					return;
				}
			}
			// Executions are being canceled. Go into cancelling and wait for
			// all vertices to be in their final state.
			else if (current == JobStatus.FAILING) {
				if (transitionState(current, JobStatus.CANCELLING)) {
					return;
				}
			}
			// All vertices have been cancelled and it's safe to directly go
			// into the canceled state.
			else if (current == JobStatus.RESTARTING) {
				synchronized (progressLock) {
					if (transitionState(current, JobStatus.CANCELED)) {
						postRunCleanup();
						progressLock.notifyAll();

						LOG.info("Canceled during restart.");
						return;
					}
				}
			}
			else {
				// no need to treat other states
				return;
			}
		}
	}
