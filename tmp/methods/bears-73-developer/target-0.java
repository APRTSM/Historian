		public void accept(Object element) {
			if (element == null || isTerminated()) {
				return;
			}
			try {
				nextStep.accept(element);
			} catch (ClassCastException e) {
				StackTraceElement[] stackEles = e.getStackTrace();
				if (stackEles.length > 1 && stackEles[0].getClassName().equals(getClass().getName()) && stackEles[0].getMethodName().equals("accept")) {
					if (Launcher.LOGGER.isTraceEnabled()) {
						//log expected CCE ... there might be some unexpected too!
						Launcher.LOGGER.trace(e);
					}
				} else {
					//Do not ignore this exception it is not expected!
					throw new SpoonException("Execution of query callback failed", e);
				}
			}
		}
