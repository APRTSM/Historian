    public boolean evaluate() {
        // disable evaluation if a fulltext index is used,
        // to avoid running out of memory if the node is large,
        // and because we might not implement all features
        // such as index aggregation
        if (selector.index instanceof FulltextQueryIndex) {
            // first verify if a property level condition exists and if that
            // condition checks out, this takes out some extra rows from the index
            // aggregation bits
            if (relativePath == null && propertyName != null) {
                PropertyValue p = selector.currentProperty(propertyName);
                if (p == null) {
                    return false;
                }
            }
            return true;
        }

        StringBuilder buff = new StringBuilder();
        if (relativePath == null && propertyName != null) {
            PropertyValue p = selector.currentProperty(propertyName);
            if (p == null) {
                return false;
            }
            appendString(buff, p);
        } else {
            String path = selector.currentPath();
            if (!PathUtils.denotesRoot(path)) {
                appendString(buff,
                        PropertyValues.newString(PathUtils.getName(path)));
            }
            if (relativePath != null) {
                path = PathUtils.concat(path, relativePath);
            }

            Tree tree = getTree(path);
            if (tree == null || !tree.exists()) {
                return false;
            }

            if (propertyName != null) {
                PropertyState p = tree.getProperty(propertyName);
                if (p == null) {
                    return false;
                }
                appendString(buff, PropertyValues.create(p));
            } else {
                for (PropertyState p : tree.getProperties()) {
                    appendString(buff, PropertyValues.create(p));
                }
            }
        }
        return getFullTextConstraint(selector).evaluate(buff.toString());
    }
