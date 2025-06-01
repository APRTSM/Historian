	public <S, C extends Serializable> OperatorState<S> getOperatorState(String name,
			S defaultState, boolean partitioned, StateCheckpointer<S, C> checkpointer) throws IOException {
		if (defaultState == null) {
			throw new RuntimeException("Cannot set default state to null.");
		}
		StreamOperatorState<S, C> state = (StreamOperatorState<S, C>) getState(name, partitioned);
		state.setDefaultState(defaultState);
		state.setCheckpointer(checkpointer);

		return (OperatorState<S>) state;
	}
