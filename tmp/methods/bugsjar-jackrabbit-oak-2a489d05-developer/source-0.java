    public void restrict(FilterImpl f) {
        if (propertyName != null) {
            if (f.getSelector().equals(selector)) {
                String p = propertyName;
                if (relativePath != null) {
                    p = PathUtils.concat(relativePath, p);
                }
                p = normalizePropertyName(p);
                restrictPropertyOnFilter(p, f);
            }
        }
        f.restrictFulltextCondition(fullTextSearchExpression.currentValue().getValue(Type.STRING));
    }
