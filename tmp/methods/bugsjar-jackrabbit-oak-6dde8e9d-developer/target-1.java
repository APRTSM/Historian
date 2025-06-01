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
    public String toString() {
        if (alwaysFalse) {
            return "Filter(always false)";
        }
        StringBuilder buff = new StringBuilder();
        buff.append("Filter(");
        if (queryStatement != null) {
            buff.append("query=").append(queryStatement);
        }
        if (fullTextConstraint != null) {
            buff.append(" fullText=").append(fullTextConstraint);
        }
        buff.append(", path=").append(getPathPlan());
        if (!propertyRestrictions.isEmpty()) {
            buff.append(", property=[");
            Iterator<Entry<String, PropertyRestriction>> iterator = propertyRestrictions
                    .entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, PropertyRestriction> p = iterator.next();
                buff.append(p.getKey()).append("=").append(p.getValue());
                if (iterator.hasNext()) {
                    buff.append(", ");
                }
            }
            buff.append("]");
        }
        buff.append(")");
        return buff.toString();
    }
