	public TypeInformation<?> specialize(ClassTypeInformation<?> type) {
		return isResolvedCompletely() ? type : super.specialize(type);
	}
