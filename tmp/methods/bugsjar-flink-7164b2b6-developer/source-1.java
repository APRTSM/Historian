	private String normalizePath(String path) {

		// remove leading and tailing whitespaces
		path = path.trim();

		// remove consecutive slashes & backslashes
		path = path.replace("\\", "/");
		path = path.replaceAll("/+", "/");

		// remove tailing separator
		if(!path.equals(SEPARATOR) && path.endsWith(SEPARATOR)) {
			path = path.substring(0, path.length() - SEPARATOR.length());
		}

		return path;
	}
	public boolean mkdirs(final Path f) throws IOException {

		final Path parent = f.getParent();
		final File p2f = pathToFile(f);
		return (parent == null || mkdirs(parent)) && (p2f.mkdir() || p2f.isDirectory());
	}
