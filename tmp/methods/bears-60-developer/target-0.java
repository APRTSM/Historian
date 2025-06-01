	public String toString() {
		DefaultJavaPrettyPrinter printer = new DefaultJavaPrettyPrinter(getFactory().getEnvironment());
		String errorMessage = "";
		try {
			printer.computeImports(this);
			printer.scan(this);
		} catch (ParentNotInitializedException ignore) {
			LOGGER.error(ERROR_MESSAGE_TO_STRING, ignore);
			errorMessage = ERROR_MESSAGE_TO_STRING;
		}
		// in line-preservation mode, newlines are added at the beginning to matches the lines
		// removing them from the toString() representation
		return printer.toString().replaceFirst("^\\s+", "") + errorMessage;
	}
