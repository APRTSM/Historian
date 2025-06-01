    public void restrict(FilterImpl f) {
        if (propertyName != null) {
            if (f.getSelector().equals(selector)) {
                String pn = normalizePropertyName(propertyName);
                f.restrictProperty(pn, Operator.NOT_EQUAL, null);
            }
        }
        f.restrictFulltextCondition(fullTextSearchExpression.currentValue().getValue(Type.STRING));
    }
