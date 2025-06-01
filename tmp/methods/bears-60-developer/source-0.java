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
		return printer.toString() + errorMessage;
	}
