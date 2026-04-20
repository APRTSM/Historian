	public int getSourceVersion() {
		String javaVersion = null;
		if (model.getBuild() != null) {
			javaVersion = getSourceVersion(model.getBuild());
		}
		if (javaVersion != null) {
			return Integer.parseInt(extractVariable(javaVersion).substring(2));
		}
		for (Profile profile: model.getProfiles()) {
			if (profile.getActivation() != null && profile.getActivation().isActiveByDefault()) {
				javaVersion = getSourceVersion(profile.getBuild());
			}
		}
		if (javaVersion != null) {
			return Integer.parseInt(extractVariable(javaVersion).substring(2));
		}
		javaVersion = getProperty("java.version");
		if (javaVersion != null) {
			return Integer.parseInt(extractVariable(javaVersion).substring(2));
		}
		javaVersion = getProperty("java.src.version");
		if (javaVersion != null) {
			return Integer.parseInt(extractVariable(javaVersion).substring(2));
		}
		javaVersion = getProperty("maven.compiler.source");
		if (javaVersion != null) {
			return Integer.parseInt(extractVariable(javaVersion).substring(2));
		}
		javaVersion = getProperty("maven.compile.source");
		if (javaVersion != null) {
			return Integer.parseInt(extractVariable(javaVersion).substring(2));
		}
		// return the current compliance level of spoon
		return environment.getComplianceLevel();
	}
