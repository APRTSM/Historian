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

			if (current == CANCELED || current == FINISHED) {
				// we are already aborting or are already aborted or we are already finished
				if (LOG.isDebugEnabled()) {
					LOG.debug("Ignoring transition of vertex {} to {} while being {}.", getVertexWithAttempt(), FAILED, current);
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
	public void fail(Throwable t) {
		while (true) {
			JobStatus current = state;
			if (current == JobStatus.FAILED || current == JobStatus.FAILING) {
				return;
			}
			else if (transitionState(current, JobStatus.FAILING, t)) {
				this.failureCause = t;

				if (!verticesInCreationOrder.isEmpty()) {
					// cancel all. what is failed will not cancel but stay failed
					for (ExecutionJobVertex ejv : verticesInCreationOrder) {
						ejv.cancel();
					}
				} else {
					// set the state of the job to failed
					transitionState(JobStatus.FAILING, JobStatus.FAILED, t);
				}

				return;
			}

			// no need to treat other states
		}
	}
