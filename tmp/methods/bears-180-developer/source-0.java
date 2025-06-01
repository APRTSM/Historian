	private BlobId getBlobId() throws IOException {
		URI uri = getURI();
		return BlobId.of(uri.getHost(),
				uri.getPath().substring(1, uri.getPath().length()));
	}
