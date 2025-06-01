		public void accept(Object element) {
			if (element == null || isTerminated()) {
				return;
			}
			try {
				nextStep.accept(element);
			} catch (ClassCastException e) {
				if (Launcher.LOGGER.isTraceEnabled()) {
					//log expected CCE ... there might be some unexpected too!
					Launcher.LOGGER.trace(e);
				}
			}
		}
