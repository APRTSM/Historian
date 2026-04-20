	private void generateNodeLocalHash(StreamNode node, Hasher hasher, int id) {
		// This resolves conflicts for otherwise identical source nodes. BUT
		// the generated hash codes depend on the ordering of the nodes in the
		// stream graph.
		hasher.putInt(id);

		hasher.putInt(node.getParallelism());

		hasher.putString(node.getOperatorName(), Charset.forName("UTF-8"));

		if (node.getOperator() instanceof AbstractUdfStreamOperator) {
			String udfClassName = ((AbstractUdfStreamOperator<?, ?>) node.getOperator())
					.getUserFunction().getClass().getName();

			hasher.putString(udfClassName, Charset.forName("UTF-8"));
		}
	}
