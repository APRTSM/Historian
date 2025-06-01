        private void syncProperties(ExternalIdentity ext, Authorizable auth, Map<String, String> mapping)
                throws RepositoryException {
            Map<String, ?> properties = ext.getProperties();
            for (Map.Entry<String, String> entry: mapping.entrySet()) {
                String relPath = entry.getKey();
                String name = entry.getValue();
                Object obj = properties.get(name);
                if (obj == null) {
                    auth.removeProperty(relPath);
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
