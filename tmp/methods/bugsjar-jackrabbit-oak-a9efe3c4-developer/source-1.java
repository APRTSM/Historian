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
        relativePath = query.getOakPath(relativePath);
        propertyName = PathUtils.getName(propertyName);
        propertyName = normalizeNonRelativePropertyName(propertyName);
        return PathUtils.concat(relativePath, propertyName);
    }
    private boolean matchesPropertyType(PropertyValue value) {
        if (value == null) {
            return false;
        }
        if (propertyType == PropertyType.UNDEFINED) {
            return true;
        }
        return value.getType().tag() == propertyType;
    }
    private boolean matchesPropertyType(PropertyState state) {
        if (state == null) {
            return false;
        }
        if (propertyType == PropertyType.UNDEFINED) {
            return true;
        }
        return state.getType().tag() == propertyType;
    }
    public PropertyValue currentProperty() {
        boolean asterisk = PathUtils.getName(propertyName).equals("*");
        if (!asterisk) {
            PropertyValue p = selector.currentProperty(propertyName);
            return matchesPropertyType(p) ? p : null;
        }
        Tree tree = selector.currentTree();
        if (tree == null || !tree.exists()) {
            return null;
        }
        if (!asterisk) {
            String name = PathUtils.getName(propertyName);
            name = normalizePropertyName(name);
            PropertyState p = tree.getProperty(name);
            if (p == null) {
                return null;
            }
            return matchesPropertyType(p) ? PropertyValues.create(p) : null;
        }
        // asterisk - create a multi-value property
        // warning: the returned property state may have a mixed type
        // (not all values may have the same type)

        // TODO currently all property values are converted to strings - 
        // this doesn't play well with the idea that the types may be different
        List<String> values = new ArrayList<String>();
        for (PropertyState p : tree.getProperties()) {
            if (matchesPropertyType(p)) {
                Iterables.addAll(values, p.getValue(Type.STRINGS));
            }
        }
        // "*"
        return PropertyValues.newString(values);
    }
