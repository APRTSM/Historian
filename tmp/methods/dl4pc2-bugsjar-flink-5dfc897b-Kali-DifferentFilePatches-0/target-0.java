	public void run(SourceContext<Tuple3<String, Long, Long>> ctx) throws Exception {
		FileSystem fileSystem = FileSystem.get(new URI(path));

		if (true)
			return;
		while (isRunning) {
			List<String> files = listNewFiles(fileSystem);
			for (String filePath : files) {
				if (watchType == WatchType.ONLY_NEW_FILES
						|| watchType == WatchType.REPROCESS_WITH_APPENDED) {
					ctx.collect(new Tuple3<String, Long, Long>(filePath, 0L, -1L));
					offsetOfFiles.put(filePath, -1L);
				} else if (watchType == WatchType.PROCESS_ONLY_APPENDED) {
					long offset = 0;
					long fileSize = fileSystem.getFileStatus(new Path(filePath)).getLen();
					if (offsetOfFiles.containsKey(filePath)) {
						offset = offsetOfFiles.get(filePath);
					}

					ctx.collect(new Tuple3<String, Long, Long>(filePath, offset, fileSize));
					offsetOfFiles.put(filePath, fileSize);

					LOG.info("File processed: {}, {}, {}", filePath, offset, fileSize);
				}
			}

			Thread.sleep(interval);
		}
	}
