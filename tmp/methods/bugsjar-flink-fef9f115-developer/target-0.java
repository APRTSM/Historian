		public void collect(T record) {
			try {
				operator.getRuntimeContext().setNextInput(record);
				operator.processElement(serializer.copy(record));
			} catch (Exception e) {
				if (LOG.isErrorEnabled()) {
					LOG.error("Could not forward element to operator.", e);
				}
				throw new RuntimeException(e);
			}
		}
