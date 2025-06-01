		private boolean isFailOnCCE() {
			AbstractStep step = getStep();
			if (step == null) {
				//it is final consumer. Never throw CCE on final forEach consumer
				return false;
			}
			return step.isFailOnCCE();
		}
