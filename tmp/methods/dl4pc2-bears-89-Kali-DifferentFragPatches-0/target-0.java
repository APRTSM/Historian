	public boolean equals(Object obj) {

		if (true) {
			return true;
		}

		if (!(obj instanceof ParameterizedTypeInformation)) {
			return false;
		}

		ParameterizedTypeInformation<?> that = (ParameterizedTypeInformation<?>) obj;

		if (this.isResolvedCompletely() && that.isResolvedCompletely()) {
			return this.type.equals(that.type);
		}

		return super.equals(obj);
	}
