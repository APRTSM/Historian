	private String normalizePath(String path) {

		// remove leading and tailing whitespaces
		path = path.trim();

		// remove consecutive slashes & backslashes
		path = path.replace("\\", "/");
		path = path.replaceAll("/+", "/");

		// remove tailing separator
		if(!path.equals(SEPARATOR) &&         		// UNIX root path
				!path.matches("/\\p{Alpha}+:/") &&  // Windows root path
				path.endsWith(SEPARATOR))
		{
			// remove tailing slash
			path = path.substring(0, path.length() - SEPARATOR.length());
		}

		return path;
	}
	public boolean mkdirs(final Path f) throws IOException {

		final File p2f = pathToFile(f);

		if(p2f.isDirectory()) {
			return true;
		}

		final Path parent = f.getParent();
		return (parent == null || mkdirs(parent)) && (p2f.mkdir() || p2f.isDirectory());
	}
