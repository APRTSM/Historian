	public void cancel() {
		// depending on the previous state, we go directly to cancelled (no cancel call necessary)
		// -- or to canceling (cancel call needs to be sent to the task manager)
		
		// because of several possibly previous states, we need to again loop until we make a
		// successful atomic state transition
		while (true) {
			
			ExecutionState current = this.state;
			
			if (current == CANCELING || current == CANCELED) {
				// already taken care of, no need to cancel again
				return;
			}
				
			// these two are the common cases where we need to send a cancel call
			else if (current == RUNNING || current == DEPLOYING) {
				// try to transition to canceling, if successful, send the cancel call
				if (transitionState(current, CANCELING)) {
					sendCancelRpcCall();
					return;
				}
				// else: fall through the loop
			}
			
			else if (current == FINISHED || current == FAILED) {
				// nothing to do any more. finished failed before it could be cancelled.
				// in any case, the task is removed from the TaskManager already
				sendFailIntermediateResultPartitionsRpcCall();

				return;
			}
			else if (current == CREATED || current == SCHEDULED) {
				// from here, we can directly switch to cancelled, because the no task has been deployed
				if (transitionState(current, CANCELED)) {
					
					// we skip the canceling state. set the timestamp, for a consistent appearance
					markTimestamp(CANCELING, getStateTimestamp(CANCELED));
					
					try {
						vertex.getExecutionGraph().deregisterExecution(this);
						if (assignedResource != null) {
							assignedResource.releaseSlot();
						}
					}
					finally {
						vertex.executionCanceled();
					}
					return;
				}
				// else: fall through the loop
			}
			else {
				throw new IllegalStateException(current.name());
			}
		}
	}
	private boolean processFail(Throwable t, boolean isCallback) {

		// damn, we failed. This means only that we keep our books and notify our parent JobExecutionVertex
		// the actual computation on the task manager is cleaned up by the TaskManager that noticed the failure

		// we may need to loop multiple times (in the presence of concurrent calls) in order to
		// atomically switch to failed 
		while (true) {
			ExecutionState current = this.state;

			if (current == FAILED) {
				// already failed. It is enough to remember once that we failed (its sad enough)
				return false;
			}

			if (current == CANCELED) {
				// we are already aborting or are already aborted
				if (LOG.isDebugEnabled()) {
					LOG.debug(String.format("Ignoring transition of vertex %s to %s while being %s", 
							getVertexWithAttempt(), FAILED, CANCELED));
				}
				return false;
			}

			if (transitionState(current, FAILED, t)) {
				// success (in a manner of speaking)
				this.failureCause = t;

				try {
					if (assignedResource != null) {
						assignedResource.releaseSlot();
					}
					vertex.getExecutionGraph().deregisterExecution(this);
				}
				finally {
					vertex.executionFailed(t);
				}
				
				if (!isCallback && (current == RUNNING || current == DEPLOYING)) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Sending out cancel request, to remove task execution from TaskManager.");
					}

					try {
						if (assignedResource != null) {
							sendCancelRpcCall();
						}
					} catch (Throwable tt) {
						// no reason this should ever happen, but log it to be safe
						LOG.error("Error triggering cancel call while marking task as failed.", tt);
					}
				}

				// leave the loop
				return true;
			}
		}
	}
	private boolean transitionState(ExecutionState currentState, ExecutionState targetState, Throwable error) {
		if (STATE_UPDATER.compareAndSet(this, currentState, targetState)) {
			markTimestamp(targetState);

			LOG.info(getVertex().getTaskNameWithSubtaskIndex() + " ("  + getAttemptId() + ") switched from "
				+ currentState + " to " + targetState);

			// make sure that the state transition completes normally.
			// potential errors (in listeners may not affect the main logic)
			try {
				vertex.notifyStateTransition(attemptId, targetState, error);
			}
			catch (Throwable t) {
				LOG.error("Error while notifying execution graph of execution state transition.", t);
			}
			return true;
		} else {
			return false;
		}
	}
