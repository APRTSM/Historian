	public void computeInterestingPropertiesForInputs(CostEstimator estimator) {
		// get what we inherit and what is preserved by our user code 
		final InterestingProperties props = getInterestingProperties().filterByCodeAnnotations(this, 0);
		
		// add all properties relevant to this node
		for (OperatorDescriptorSingle dps : getPossibleProperties()) {
			for (RequestedGlobalProperties gp : dps.getPossibleGlobalProperties()) {
				props.addGlobalProperties(gp);
			}
			for (RequestedLocalProperties lp : dps.getPossibleLocalProperties()) {
				props.addLocalProperties(lp);
			}
		}
		this.inConn.setInterestingProperties(props);
		
		for (PactConnection conn : getBroadcastConnections()) {
			conn.setInterestingProperties(new InterestingProperties());
		}
	}
