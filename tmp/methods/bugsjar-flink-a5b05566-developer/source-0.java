	public static InfoOptions parseInfoCommand(String[] args) throws CliArgsException {
		try {
			PosixParser parser = new PosixParser();
			CommandLine line = parser.parse(INFO_OPTIONS, args, false);
			return new InfoOptions(line);
		}
		catch (ParseException e) {
			throw new CliArgsException(e.getMessage());
		}
	}
