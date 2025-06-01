	private List<String> listNewFiles(FileSystem fileSystem) throws IOException {
		List<String> files = new ArrayList<String>();

		FileStatus[] statuses = fileSystem.listStatus(new Path(path));

		if (statuses == null) {
			LOG.warn("Path does not exist: {}", path);
		} else {
			for (FileStatus status : statuses) {
				Path filePath = status.getPath();
				String fileName = filePath.getName();
				long modificationTime = status.getModificationTime();

				if (!isFiltered(fileName, modificationTime)) {
					files.add(filePath.toString());
					modificationTimes.put(fileName, modificationTime);
				}
			}
		}

		return files;
	}
