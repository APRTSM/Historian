	private BlobId getBlobId() throws IOException {
		URI uri = getURI();
		return BlobId.of(uri.getAuthority(),
				uri.getPath().substring(1, uri.getPath().length()));
	}
