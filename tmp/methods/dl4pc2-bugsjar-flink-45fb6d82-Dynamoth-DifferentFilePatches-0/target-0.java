	public void computeInterestingPropertiesForInputs(CostEstimator estimator) {
		// get what we inherit and what is preserved by our user code 
		final InterestingProperties props = getInterestingProperties().filterByCodeAnnotations(this, 0);
		
		// add all properties relevant to this node
		for (OperatorDescriptorSingle dps : getPossibleProperties()) {
			for (RequestedGlobalProperties gp : dps.getPossibleGlobalProperties()) {
				
				if (gp.getPartitioning().isPartitionedOnKey()) {
					// make sure that among the same partitioning types, we do not push anything down that has fewer key fields
					
					for (RequestedGlobalProperties contained : props.getGlobalProperties()) {
						if (contained.getPartitioning() == gp.getPartitioning() && gp.getPartitionedFields().isValidSubset(contained.getPartitionedFields())) {
							if (this.getMaxDepth() == 1) {
								props.getGlobalProperties().remove(contained);
							}
							break;
						}
					}
				}
				
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
