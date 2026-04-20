    public void restrict(FilterImpl f) {
        f.restrictFulltextCondition(fullTextSearchExpression.currentValue().getValue(Type.STRING));
    }
