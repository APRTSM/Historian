	protected void prunePlanAlternativesWithCommonBranching(List<PlanNode> plans) {
		// for each interesting property, which plans are cheapest
		final RequestedGlobalProperties[] gps = (RequestedGlobalProperties[]) this.intProps.getGlobalProperties().toArray(new RequestedGlobalProperties[this.intProps.getGlobalProperties().size()]);
		final RequestedLocalProperties[] lps = (RequestedLocalProperties[]) this.intProps.getLocalProperties().toArray(new RequestedLocalProperties[this.intProps.getLocalProperties().size()]);
		
		final PlanNode[][] toKeep = new PlanNode[gps.length][];
		final PlanNode[] cheapestForGlobal = new PlanNode[gps.length];
		
		
		PlanNode cheapest = null; // the overall cheapest plan

		// go over all plans from the list
		for (PlanNode candidate : plans) {
			// check if that plan is the overall cheapest
			if (cheapest == null || (cheapest.getCumulativeCosts().compareTo(candidate.getCumulativeCosts()) > 0)) {
				cheapest = candidate;
			}

			// find the interesting global properties that this plan matches
			for (int i = 0; i < gps.length; i++) {
				if (gps[i].isMetBy(candidate.getGlobalProperties())) {
					// the candidate meets the global property requirements. That means
					// it has a chance that its local properties are re-used (they would be
					// destroyed if global properties need to be established)
					
					if (cheapestForGlobal[i] == null || (cheapestForGlobal[i].getCumulativeCosts().compareTo(candidate.getCumulativeCosts()) > 0)) {
						cheapestForGlobal[i] = candidate;
					}
					
					final PlanNode[] localMatches;
					if (toKeep[i] == null) {
						localMatches = new PlanNode[lps.length];
						toKeep[i] = localMatches;
					} else {
						localMatches = toKeep[i];
					}
					
					for (int k = 0; k < lps.length; k++) {
						if (lps[k].isMetBy(candidate.getLocalProperties())) {
							final PlanNode previous = localMatches[k];
							if (previous == null || previous.getCumulativeCosts().compareTo(candidate.getCumulativeCosts()) > 0) {
								// this one is cheaper!
								localMatches[k] = candidate;
							}
						}
					}
				}
			}
		}

		// all plans are set now
		plans.clear();

		// add the cheapest plan
		if (cheapest != null) {
			plans.add(cheapest);
			cheapest.setPruningMarker(); // remember that that plan is in the set
		}
		
		// skip the top down delta cost check for now (TODO: implement this)
		// add all others, which are optimal for some interesting properties
		for (int i = 0; i < gps.length; i++) {
			if (toKeep[i] != null) {
				final PlanNode[] localMatches = toKeep[i];
				for (int k = 0; k < localMatches.length; k++) {
					final PlanNode n = localMatches[k];
					if (n != null && !n.isPruneMarkerSet()) {
						n.setPruningMarker();
						plans.add(n);
					}
				}
			}
			if (cheapestForGlobal[i] != null) {
				final PlanNode n = cheapestForGlobal[i];
				if (!n.isPruneMarkerSet()) {
					n.setPruningMarker();
					plans.add(n);
				}
			}
		}
	}
