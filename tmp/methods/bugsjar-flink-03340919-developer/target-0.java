	public FileInputSplit[] createInputSplits(int minNumSplits) throws IOException {
		if (minNumSplits < 1) {
			throw new IllegalArgumentException("Number of input splits has to be at least 1.");
		}
		
		// take the desired number of splits into account
		minNumSplits = Math.max(minNumSplits, this.numSplits);
		
		final Path path = this.filePath;
		final List<FileInputSplit> inputSplits = new ArrayList<FileInputSplit>(minNumSplits);

		// get all the files that are involved in the splits
		List<FileStatus> files = new ArrayList<FileStatus>();
		long totalLength = 0;

		final FileSystem fs = path.getFileSystem();
		final FileStatus pathFile = fs.getFileStatus(path);

		if (pathFile.isDir()) {
			totalLength += addFilesInDir(path, files, true);
		} else {
			testForUnsplittable(pathFile);

			files.add(pathFile);
			totalLength += pathFile.getLen();
		}
		// returns if unsplittable
		if(unsplittable) {
			int splitNum = 0;
			for (final FileStatus file : files) {
				final BlockLocation[] blocks = fs.getFileBlockLocations(file, 0, file.getLen());
				Set<String> hosts = new HashSet<String>();
				for(BlockLocation block : blocks) {
					hosts.addAll(Arrays.asList(block.getHosts()));
				}
				long len = file.getLen();
				if(testForUnsplittable(file)) {
					len = READ_WHOLE_SPLIT_FLAG;
				}
				FileInputSplit fis = new FileInputSplit(splitNum++, file.getPath(), 0, len,
						hosts.toArray(new String[hosts.size()]));
				inputSplits.add(fis);
			}
			return inputSplits.toArray(new FileInputSplit[inputSplits.size()]);
		}
		

		final long maxSplitSize = (minNumSplits < 1) ? Long.MAX_VALUE : (totalLength / minNumSplits +
					(totalLength % minNumSplits == 0 ? 0 : 1));

		// now that we have the files, generate the splits
		int splitNum = 0;
		for (final FileStatus file : files) {

			final long len = file.getLen();
			final long blockSize = file.getBlockSize();
			
			final long minSplitSize;
			if (this.minSplitSize <= blockSize) {
				minSplitSize = this.minSplitSize;
			}
			else {
				if (LOG.isWarnEnabled()) {
					LOG.warn("Minimal split size of " + this.minSplitSize + " is larger than the block size of " + 
						blockSize + ". Decreasing minimal split size to block size.");
				}
				minSplitSize = blockSize;
			}

			final long splitSize = Math.max(minSplitSize, Math.min(maxSplitSize, blockSize));
			final long halfSplit = splitSize >>> 1;

			final long maxBytesForLastSplit = (long) (splitSize * MAX_SPLIT_SIZE_DISCREPANCY);

			if (len > 0) {

				// get the block locations and make sure they are in order with respect to their offset
				final BlockLocation[] blocks = fs.getFileBlockLocations(file, 0, len);
				Arrays.sort(blocks);

				long bytesUnassigned = len;
				long position = 0;

				int blockIndex = 0;

				while (bytesUnassigned > maxBytesForLastSplit) {
					// get the block containing the majority of the data
					blockIndex = getBlockIndexForPosition(blocks, position, halfSplit, blockIndex);
					// create a new split
					FileInputSplit fis = new FileInputSplit(splitNum++, file.getPath(), position, splitSize,
						blocks[blockIndex].getHosts());
					inputSplits.add(fis);

					// adjust the positions
					position += splitSize;
					bytesUnassigned -= splitSize;
				}

				// assign the last split
				if (bytesUnassigned > 0) {
					blockIndex = getBlockIndexForPosition(blocks, position, halfSplit, blockIndex);
					final FileInputSplit fis = new FileInputSplit(splitNum++, file.getPath(), position,
						bytesUnassigned, blocks[blockIndex].getHosts());
					inputSplits.add(fis);
				}
			} else {
				// special case with a file of zero bytes size
				final BlockLocation[] blocks = fs.getFileBlockLocations(file, 0, 0);
				String[] hosts;
				if (blocks.length > 0) {
					hosts = blocks[0].getHosts();
				} else {
					hosts = new String[0];
				}
				final FileInputSplit fis = new FileInputSplit(splitNum++, file.getPath(), 0, 0, hosts);
				inputSplits.add(fis);
			}
		}

		return inputSplits.toArray(new FileInputSplit[inputSplits.size()]);
	}
	private long addFilesInDir(Path path, List<FileStatus> files, boolean logExcludedFiles)
			throws IOException {
		final FileSystem fs = path.getFileSystem();

		long length = 0;

		for(FileStatus dir: fs.listStatus(path)) {
			if (dir.isDir()) {
				if (acceptFile(dir) && enumerateNestedFiles) {
					length += addFilesInDir(dir.getPath(), files, logExcludedFiles);
				} else {
					if (logExcludedFiles && LOG.isDebugEnabled()) {
						LOG.debug("Directory "+dir.getPath().toString()+" did not pass the file-filter and is excluded.");
					}
				}
			}
			else {
				if(acceptFile(dir)) {
					files.add(dir);
					length += dir.getLen();
					testForUnsplittable(dir);
				} else {
					if (logExcludedFiles && LOG.isDebugEnabled()) {
						LOG.debug("Directory "+dir.getPath().toString()+" did not pass the file-filter and is excluded.");
					}
				}
			}
		}
		return length;
	}
	protected FileBaseStatistics getFileStats(FileBaseStatistics cachedStats, Path filePath, FileSystem fs,
			ArrayList<FileStatus> files) throws IOException {
		
		// get the file info and check whether the cached statistics are still valid.
		final FileStatus file = fs.getFileStatus(filePath);
		long totalLength = 0;

		// enumerate all files
		if (file.isDir()) {
			totalLength += addFilesInDir(file.getPath(), files, false);
		} else {
			files.add(file);
			testForUnsplittable(file);
			totalLength += file.getLen();
		}

		// check the modification time stamp
		long latestModTime = 0;
		for (FileStatus f : files) {
			latestModTime = Math.max(f.getModificationTime(), latestModTime);
		}

		// check whether the cached statistics are still valid, if we have any
		if (cachedStats != null && latestModTime <= cachedStats.getLastModificationTime()) {
			return cachedStats;
		}

		// sanity check
		if (totalLength <= 0) {
			totalLength = BaseStatistics.SIZE_UNKNOWN;
		}
		return new FileBaseStatistics(latestModTime, totalLength, BaseStatistics.AVG_RECORD_BYTES_UNKNOWN);
	}
