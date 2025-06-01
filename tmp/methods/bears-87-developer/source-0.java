		protected Object wrap(Object source) {
			return ReflectionUtils.invokeMethod(OF_METHOD, source);
		}
