    public ClassGraph blacklistLibOrExtJars(final String... jarLeafNames) {
        if (jarLeafNames.length == 0) {
            // Blacklist all lib or ext jars
            for (final String libOrExtJar : JarUtils.getJreLibOrExtJars()) {
                blacklistLibOrExtJars(JarUtils.leafName(libOrExtJar));
            }
        } else {
            for (final String jarLeafName : jarLeafNames) {
                final String leafName = JarUtils.leafName(jarLeafName);
                if (!leafName.equals(jarLeafName)) {
                    throw new IllegalArgumentException("Can only blacklist jars by leafname: " + jarLeafName);
                }
                if (jarLeafName.contains("*")) {
                    // Compare wildcarded pattern against all jars in lib and ext dirs 
                    final Pattern pattern = WhiteBlackList.globToPattern(jarLeafName);
                    boolean found = false;
                    for (final String libOrExtJarPath : JarUtils.getJreLibOrExtJars()) {
                        final String libOrExtJarLeafName = JarUtils.leafName(libOrExtJarPath);
                        if (pattern.matcher(libOrExtJarLeafName).matches()) {
                            // Check for "*" in filename to prevent infinite recursion (shouldn't happen)
                            if (!libOrExtJarLeafName.contains("*")) {
                                blacklistLibOrExtJars(libOrExtJarLeafName);
                            }
                            found = true;
                        }
                    }
                    if (!found && topLevelLog != null) {
                        topLevelLog.log("Could not find lib or ext jar matching wildcard: " + jarLeafName);
                    }
                } else {
                    // No wildcards, just blacklist the named jar, if present
                    boolean found = false;
                    for (final String libOrExtJarPath : JarUtils.getJreLibOrExtJars()) {
                        final String libOrExtJarLeafName = JarUtils.leafName(libOrExtJarPath);
                        if (jarLeafName.equals(libOrExtJarLeafName)) {
                            scanSpec.libOrExtJarWhiteBlackList.addToBlacklist(jarLeafName);
                            if (topLevelLog != null) {
                                topLevelLog.log("Blacklisting lib or ext jar: " + libOrExtJarPath);
                            }
                            found = true;
                            break;
                        }
                    }
                    if (!found && topLevelLog != null) {
                        topLevelLog.log("Could not find lib or ext jar: " + jarLeafName);
                    }
                }
            }
        }
        return this;
    }
