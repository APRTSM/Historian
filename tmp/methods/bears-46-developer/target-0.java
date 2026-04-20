	public Set<String> getMetadataKeys() {
		if (metadata == null) {
			return Collections.EMPTY_SET;
		}
		return metadata.keySet();
	}
