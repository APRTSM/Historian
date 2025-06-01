	private boolean canBeProcessed(CtElement e) {
		if (!factory.getEnvironment().isProcessingStopped()
				&& processor.getProcessedElementTypes() != null) {
			for (Object o : processor.getProcessedElementTypes()) {
				if (!((Class<?>) o).isAssignableFrom(e.getClass())) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
