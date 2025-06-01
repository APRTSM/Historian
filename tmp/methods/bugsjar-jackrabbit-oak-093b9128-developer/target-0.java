        private void syncProperties(ExternalIdentity ext, Authorizable auth, Map<String, String> mapping)
                throws RepositoryException {
            Map<String, ?> properties = ext.getProperties();
            for (Map.Entry<String, String> entry: mapping.entrySet()) {
                String relPath = entry.getKey();
                String name = entry.getValue();
                Object obj = properties.get(name);
                if (obj == null) {
                    int nameLen = name.length();
                    if (nameLen > 1 && name.charAt(0) == '"' && name.charAt(nameLen-1) == '"') {
                        auth.setProperty(relPath, valueFactory.createValue(name.substring(1, nameLen - 1)));
                    } else {
                        auth.removeProperty(relPath);
                    }
                } else {
                    if (obj instanceof Collection) {
                        auth.setProperty(relPath, createValues((Collection) obj));
                    } else if (obj instanceof byte[] || obj instanceof char[]) {
                        auth.setProperty(relPath, createValue(obj));
                    } else if (obj instanceof Object[]) {
                        auth.setProperty(relPath, createValues(Arrays.asList((Object[]) obj)));
                    } else {
                        auth.setProperty(relPath, createValue(obj));
                    }
                }
            }
        }
