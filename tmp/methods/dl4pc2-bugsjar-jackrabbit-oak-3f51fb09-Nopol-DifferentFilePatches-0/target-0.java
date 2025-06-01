    public String getJcrName(String oakName) {
        checkNotNull(oakName);
        checkArgument(!oakName.startsWith(":")); // hidden name
        checkArgument(!oakName.startsWith("{")); // expanded name

        if (hasSessionLocalMappings()) {
            int colon = oakName.indexOf(':');
            if (colon > 0) {
                String oakPrefix = oakName.substring(0, colon);
                String uri = getNamespaceMap().get(oakPrefix);
                if (uri == null) {
                    throw new IllegalStateException(
                            "No namespace mapping found for " + oakName);
                }

                for (Map.Entry<String, String> entry : local.entrySet()) {
                    if (uri.equals(entry.getValue())) {
                        String jcrPrefix = entry.getKey();
                        if (jcrPrefix.equals(oakPrefix)) {
                            return oakName;
                        } else {
                            if ((oakPrefix.length()) != (jcrPrefix.length())) {
                                return jcrPrefix + oakName.substring(colon);
                            }
                        }
                    }
                }

                // local mapping not found for this URI, make sure there
                // is no conflicting local mapping for the prefix
                if (local.containsKey(oakPrefix)) {
                    for (int i = 2; true; i++) {
                        String jcrPrefix = oakPrefix + i;
                        if (!local.containsKey(jcrPrefix)) {
                            local.put(jcrPrefix, uri);
                            return jcrPrefix + oakName.substring(colon);
                        }
                    }
                }
            }
        }

        return oakName;
    }
