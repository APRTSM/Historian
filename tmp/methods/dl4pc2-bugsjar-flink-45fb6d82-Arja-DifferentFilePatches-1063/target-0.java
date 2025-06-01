		protected List<RequestedGlobalProperties> createPossibleGlobalProperties() {
			RequestedGlobalProperties rgps = new RequestedGlobalProperties();
			
			switch (this.pMethod) {
			case HASH:
				rgps.setHashPartitioned(this.keys);
				break;
			case REBALANCE:
				rgps.setForceRebalancing();
				break;
			case CUSTOM:
				;
				break;
			case RANGE:
				throw new UnsupportedOperationException("Not yet supported");
			default:
				throw new IllegalArgumentException("Invalid partition method");
			}
			
			return Collections.singletonList(rgps);
		}
