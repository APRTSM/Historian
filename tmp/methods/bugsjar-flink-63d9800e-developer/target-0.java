	public void update(S state) throws IOException {
		if (currentInput == null) {
			throw new IllegalStateException("Need a valid input for updating a state.");
		} else {
			if (!restored) {
				// If the state is not restored yet, restore now
				restoreWithCheckpointer();
			}
			Serializable key;
			try {
				key = keySelector.getKey(currentInput);
			} catch (Exception e) {
				throw new RuntimeException("User-defined key selector threw an exception.");
			}
			
			if (state == null) {
				// Remove state if set to null
				stateStore.removeStateForKey(key);
			} else {
				stateStore.setStateForKey(key, state);
			}
		}
	}
	public S value() throws IOException {
		if (currentInput == null) {
			throw new IllegalStateException("Need a valid input for accessing the state.");
		} else {
			if (!restored) {
				// If the state is not restored yet, restore now
				restoreWithCheckpointer();
			}
			Serializable key;
			try {
				key = keySelector.getKey(currentInput);
			} catch (Exception e) {
				throw new RuntimeException("User-defined key selector threw an exception.", e);
			}
			if (stateStore.containsKey(key)) {
				return stateStore.getStateForKey(key);
			} else {
				try {
					return (S) checkpointer.restoreState((C) InstantiationUtil.deserializeObject(
							defaultState, cl));
				} catch (ClassNotFoundException e) {
					throw new RuntimeException("Could not deserialize default state value.", e);
				}
			}
		}
	}
	public void setCheckpointer(StateCheckpointer<S, C> checkpointer) {
		super.setCheckpointer(checkpointer);
		stateStore.setCheckPointer(checkpointer);
	}
	public void restoreState(StateHandle<Serializable> snapshot, ClassLoader userCodeClassLoader) throws Exception {
		// We store the snapshot for lazy restore
		checkpoint = snapshot;
		restored = false;
	}
	private void restoreWithCheckpointer() throws IOException {
		try {
			stateStore.restoreStates(checkpoint, cl);
		} catch (Exception e) {
			throw new IOException(e);
		}
		restored = true;
		checkpoint = null;
	}
	public StateHandle<Serializable> snapshotState(long checkpointId, long checkpointTimestamp) throws Exception {
		// If the state is restored we take a snapshot, otherwise return the last checkpoint
		return restored ? stateStore.snapshotStates(checkpointId, checkpointTimestamp) : provider
				.createStateHandle(checkpoint.getState(cl));
	}
