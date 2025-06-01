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
    private static void addToUnionList(Expression condition,  ArrayList<Expression> unionList) {
        if (condition instanceof OrCondition) {
            OrCondition or = (OrCondition) condition;
            // conditions of type                
            // @x = 1 or @y = 2
            // or similar are converted to
            // (@x = 1) union (@y = 2)
            addToUnionList(or.left, unionList);
            addToUnionList(or.right, unionList);
            return;
        } else if (condition instanceof AndCondition) {
            // conditions of type
            // @a = 1 and (@x = 1 or @y = 2)
            // are automatically converted to
            // (@a = 1 and @x = 1) union (@a = 1 and @y = 2)
            AndCondition and = (AndCondition) condition;
            and = and.pullOrRight();
            if (and.right instanceof OrCondition) {
                OrCondition or = (OrCondition) and.right;
                // same as above, but with the added "and"
                addToUnionList(new AndCondition(and.left, or.left), unionList);
                addToUnionList(new AndCondition(and.left, or.right), unionList);
                return;
            }
        }
        unionList.add(condition);
    }
