    public void restrict(FilterImpl f) {
        if (propertyName != null) {
            if (f.getSelector().equals(selector)) {
                String p = propertyName;
                if (relativePath != null) {
                    p = PathUtils.concat(p, relativePath);
                }
                p = normalizePropertyName(p);
                f.restrictProperty(p, Operator.NOT_EQUAL, null);
            }
        }
        f.restrictFulltextCondition(fullTextSearchExpression.currentValue().getValue(Type.STRING));
    }
