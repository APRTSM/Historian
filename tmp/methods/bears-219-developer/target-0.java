	public int getSourceVersion() {
		String javaVersion = null;
		if (model.getBuild() != null) {
			javaVersion = getSourceVersion(model.getBuild());
		}
		if (javaVersion != null) {
			return correctJavaVersion(javaVersion);
		}
		for (Profile profile: model.getProfiles()) {
			if (profile.getActivation() != null && profile.getActivation().isActiveByDefault()) {
				javaVersion = getSourceVersion(profile.getBuild());
			}
		}
		if (javaVersion != null) {
			return correctJavaVersion(javaVersion);
		}
		javaVersion = getProperty("java.version");
		if (javaVersion != null) {
			return correctJavaVersion(javaVersion);
		}
		javaVersion = getProperty("java.src.version");
		if (javaVersion != null) {
			return correctJavaVersion(javaVersion);
		}
		javaVersion = getProperty("maven.compiler.source");
		if (javaVersion != null) {
			return correctJavaVersion(javaVersion);
		}
		javaVersion = getProperty("maven.compile.source");
		if (javaVersion != null) {
			return correctJavaVersion(javaVersion);
		}
		// return the current compliance level of spoon
		return environment.getComplianceLevel();
	}
	private int correctJavaVersion(String javaVersion) {
		String version = extractVariable(javaVersion);
		return Integer.parseInt((version.contains(".") ? version.substring(2) : version));
	}
