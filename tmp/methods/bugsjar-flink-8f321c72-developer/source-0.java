	public Path(String pathString) {
		checkPathArg(pathString);

		// We can't use 'new URI(String)' directly, since it assumes things are
		// escaped, which we don't require of Paths.

		// add a slash in front of paths with Windows drive letters
		if (hasWindowsDrive(pathString, false)) {
			pathString = "/" + pathString;
		}

		// parse uri components
		String scheme = null;
		String authority = null;

		int start = 0;

		// parse uri scheme, if any
		final int colon = pathString.indexOf(':');
		final int slash = pathString.indexOf('/');
		if ((colon != -1) && ((slash == -1) || (colon < slash))) { // has a
			// scheme
			scheme = pathString.substring(0, colon);
			start = colon + 1;
		}

		// parse uri authority, if any
		if (pathString.startsWith("//", start) && (pathString.length() - start > 2)) { // has authority
			final int nextSlash = pathString.indexOf('/', start + 2);
			final int authEnd = nextSlash > 0 ? nextSlash : pathString.length();
			authority = pathString.substring(start + 2, authEnd);
			start = authEnd;
		}
	private String normalizePath(String path) {
		// remove double slashes & backslashes
		path = path.replace("//", "/");
		path = path.replace("\\", "/");

		return path;
	}
	public String getName() {
		final String path = uri.getPath();
		if (path.endsWith(SEPARATOR)) {
			final int slash = path.lastIndexOf(SEPARATOR, path.length() - SEPARATOR.length() - 1);
			return path.substring(slash + 1, path.length() - SEPARATOR.length());
		} else {
			final int slash = path.lastIndexOf(SEPARATOR);
			return path.substring(slash + 1);
		}
	}
	public Path(String scheme, String authority, String path) {
		checkPathArg(path);
		initialize(scheme, authority, path);
	}
	private void checkPathArg(String path) {
		// disallow construction of a Path from an empty string
		if (path == null) {
			throw new IllegalArgumentException("Can not create a Path from a null string");
		}
		if (path.length() == 0) {
			throw new IllegalArgumentException("Can not create a Path from an empty string");
		}
	}
