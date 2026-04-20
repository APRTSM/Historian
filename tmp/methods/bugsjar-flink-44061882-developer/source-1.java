	public void attachJobGraph(List<JobVertex> topologiallySorted) throws JobException {
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("Attaching %d topologically sorted vertices to existing job graph with %d "
					+ "vertices and %d intermediate results.", topologiallySorted.size(), tasks.size(), intermediateResults.size()));
		}

		final long createTimestamp = System.currentTimeMillis();

		for (JobVertex jobVertex : topologiallySorted) {

			// create the execution job vertex and attach it to the graph
			ExecutionJobVertex ejv = new ExecutionJobVertex(this, jobVertex, 1, timeout, createTimestamp);
			ejv.connectToPredecessors(this.intermediateResults);

			ExecutionJobVertex previousTask = this.tasks.putIfAbsent(jobVertex.getID(), ejv);
			if (previousTask != null) {
				throw new JobException(String.format("Encountered two job vertices with ID %s : previous=[%s] / new=[%s]",
						jobVertex.getID(), ejv, previousTask));
			}

			for (IntermediateResult res : ejv.getProducedDataSets()) {
				IntermediateResult previousDataSet = this.intermediateResults.putIfAbsent(res.getId(), res);
				if (previousDataSet != null) {
					throw new JobException(String.format("Encountered two intermediate data set with ID %s : previous=[%s] / new=[%s]",
							res.getId(), res, previousDataSet));
				}
			}

			this.verticesInCreationOrder.add(ejv);
		}
	}
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
	public void resetForNewExecution() {
		if (!(numSubtasksInFinalState == 0 || numSubtasksInFinalState == parallelism)) {
			throw new IllegalStateException("Cannot reset vertex that is not in final state");
		}
		
		synchronized (stateMonitor) {
			// check and reset the sharing groups with scheduler hints
			if (slotSharingGroup != null) {
				slotSharingGroup.clearTaskAssignment();
			}
			if (coLocationGroup != null) {
				coLocationGroup.resetConstraints();
			}
			
			// reset vertices one by one. if one reset fails, the "vertices in final state"
			// fields will be consistent to handle triggered cancel calls
			for (int i = 0; i < parallelism; i++) {
				taskVertices[i].resetForNewExecution();
				if (finishedSubtasks[i]) {
					finishedSubtasks[i] = false;
					numSubtasksInFinalState--;
				}
			}
			
			if (numSubtasksInFinalState != 0) {
				throw new RuntimeException("Bug: resetting the execution job vertex failed.");
			}
			
			// set up the input splits again
			try {
				if (this.inputSplits != null) {
					
					if (inputSplitsPerSubtask == null) {
						// lazy assignment
						@SuppressWarnings("unchecked")
						InputSplitSource<InputSplit> splitSource = (InputSplitSource<InputSplit>) jobVertex.getInputSplitSource();
						this.splitAssigner = splitSource.getInputSplitAssigner(this.inputSplits);
					}
					else {
						// eager assignment
						//TODO: this.splitAssigner = new AssignBasedOnPreAssignment();
					}
				}
			}
			catch (Throwable t) {
				throw new RuntimeException("Re-creating the input split assigner failed: " + t.getMessage(), t);
			}

			// Reset intermediate results
			for (IntermediateResult result : producedDataSets) {
				result.resetForNewExecution();
			}
		}
	}
