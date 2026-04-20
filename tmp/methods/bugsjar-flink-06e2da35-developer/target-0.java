	public boolean triggerCheckpoint(final long timestamp) {
		if (shutdown) {
			LOG.error("Cannot trigger checkpoint, checkpoint coordinator has been shutdown.");
			return false;
		}
		
		final long checkpointID = checkpointIdCounter.getAndIncrement();
		LOG.info("Triggering checkpoint " + checkpointID + " @ " + timestamp);
		
		try {
			// first check if all tasks that we need to trigger are running.
			// if not, abort the checkpoint
			ExecutionAttemptID[] triggerIDs = new ExecutionAttemptID[tasksToTrigger.length];
			for (int i = 0; i < tasksToTrigger.length; i++) {
				Execution ee = tasksToTrigger[i].getCurrentExecutionAttempt();
				if (ee != null && ee.getState() == ExecutionState.RUNNING) {
					triggerIDs[i] = ee.getAttemptId();
				} else {
					LOG.info("Checkpoint triggering task {} is not being executed at the moment. Aborting checkpoint.",
							tasksToTrigger[i].getSimpleName());
					return false;
				}
			}

			// next, check if all tasks that need to acknowledge the checkpoint are running.
			// if not, abort the checkpoint
			Map<ExecutionAttemptID, ExecutionVertex> ackTasks =
								new HashMap<ExecutionAttemptID, ExecutionVertex>(tasksToWaitFor.length);

			for (ExecutionVertex ev : tasksToWaitFor) {
				Execution ee = ev.getCurrentExecutionAttempt();
				if (ee != null) {
					ackTasks.put(ee.getAttemptId(), ev);
				} else {
					LOG.info("Checkpoint acknowledging task {} is not being executed at the moment. Aborting checkpoint.",
							ev.getSimpleName());
					return false;
				}
			}
			
			// register a new pending checkpoint. this makes sure we can properly receive acknowledgements
			final PendingCheckpoint checkpoint = new PendingCheckpoint(job, checkpointID, timestamp, ackTasks);

			// schedule the timer that will clean up the expired checkpoints
			TimerTask canceller = new TimerTask() {
				@Override
				public void run() {
					try {
						synchronized (lock) {
							// only do the work if the checkpoint is not discarded anyways
							// note that checkpoint completion discards the pending checkpoint object
							if (!checkpoint.isDiscarded()) {
								LOG.info("Checkpoint " + checkpointID + " expired before completing.");
								
								checkpoint.discard(userClassLoader, true);
								
								pendingCheckpoints.remove(checkpointID);
								rememberRecentCheckpointId(checkpointID);
							}
						}
					}
					catch (Throwable t) {
						LOG.error("Exception while handling checkpoint timeout", t);
					}
				}
			};
			
			synchronized (lock) {
				if (shutdown) {
					throw new IllegalStateException("Checkpoint coordinator has been shutdown.");
				}
				pendingCheckpoints.put(checkpointID, checkpoint);
				timer.schedule(canceller, checkpointTimeout);
			}

			// send the messages to the tasks that trigger their checkpoint
			for (int i = 0; i < tasksToTrigger.length; i++) {
				ExecutionAttemptID id = triggerIDs[i];
				TriggerCheckpoint message = new TriggerCheckpoint(job, id, checkpointID, timestamp);
				tasksToTrigger[i].sendMessageToCurrentExecution(message, id);
			}
			
			numUnsuccessfulCheckpointsTriggers.set(0);
			return true;
		}
		catch (Throwable t) {
			int numUnsuccessful = numUnsuccessfulCheckpointsTriggers.incrementAndGet();
			LOG.warn("Failed to trigger checkpoint (" + numUnsuccessful + " consecutive failed attempts so far)", t);
			
			synchronized (lock) {
				PendingCheckpoint checkpoint = pendingCheckpoints.remove(checkpointID);
				if (checkpoint != null && !checkpoint.isDiscarded()) {
					checkpoint.discard(userClassLoader, true);
				}
			}
			
			return false;
		}
	}
