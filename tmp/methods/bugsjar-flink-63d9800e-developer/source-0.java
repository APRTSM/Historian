	public void restoreState(StateHandle<Serializable> snapshots, ClassLoader userCodeClassLoader) throws Exception {
		stateStore.restoreStates(snapshots, userCodeClassLoader);
	}
	public StateHandle<Serializable> snapshotState(long checkpointId, long checkpointTimestamp) throws Exception {
		return stateStore.snapshotStates(checkpointId, checkpointTimestamp);
	}
	public void update(S state) throws IOException {
		if (currentInput == null) {
			throw new IllegalStateException("Need a valid input for updating a state.");
		} else {
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
