    protected String normalizePropertyName(String propertyName) {
        // TODO normalize the path (remove superfluous ".." and "." 
        // where possible)
        if (query == null) {
            return propertyName;
        }
        if (propertyName == null) {
            return null;
        }
        int slash = propertyName.indexOf('/');
        if (slash < 0) {
            return normalizeNonRelativePropertyName(propertyName);
        }
        // relative properties
        String relativePath = PathUtils.getParentPath(propertyName);
        if (relativePath.indexOf('*') >= 0) {
            StringBuilder buff = new StringBuilder();
            for (String p : PathUtils.elements(relativePath)) {
                if (!p.equals("*")) {
                    p = query.getOakPath(p);
                }
                if (p.length() > 0) {
                    if (buff.length() > 0) {
                        buff.append('/');
                    }
                    buff.append(p);
                }
            }
            relativePath = buff.toString();
        } else {
            relativePath = query.getOakPath(relativePath);
        }
        propertyName = PathUtils.getName(propertyName);
        propertyName = normalizeNonRelativePropertyName(propertyName);
        return PathUtils.concat(relativePath, propertyName);
    }
