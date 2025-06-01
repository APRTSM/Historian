    public String toString() {
        StringBuilder buff = new StringBuilder();
        
        // explain | measure ...
        if (explain) {
            buff.append("explain ");
        } else if (measure) {
            buff.append("measure ");
        }
        
        // select ...
        buff.append("select ");
        buff.append(new Expression.Property(columnSelector, QueryImpl.JCR_PATH, false).toString());
        if (selectors.size() > 1) {
            buff.append(" as ").append('[').append(QueryImpl.JCR_PATH).append(']');
        }
        buff.append(", ");
        buff.append(new Expression.Property(columnSelector, QueryImpl.JCR_SCORE, false).toString());
        if (selectors.size() > 1) {
            buff.append(" as ").append('[').append(QueryImpl.JCR_SCORE).append(']');
        }
        if (columnList.isEmpty()) {
            buff.append(", ");
            buff.append(new Expression.Property(columnSelector, "*", false).toString());
        } else {
            for (int i = 0; i < columnList.size(); i++) {
                buff.append(", ");
                Expression e = columnList.get(i);
                String columnName = e.toString();
                buff.append(columnName);
                if (selectors.size() > 1) {
                    buff.append(" as [").append(e.getColumnAliasName()).append("]");
                }
            }
        }
        
        // from ...
        buff.append(" from ");
        for (int i = 0; i < selectors.size(); i++) {
            Selector s = selectors.get(i);
            if (i > 0) {
                buff.append(" inner join ");
            }
            String nodeType = s.nodeType;
            if (nodeType == null) {
                nodeType = "nt:base";
            }
            buff.append('[' + nodeType + ']').append(" as ").append(s.name);
            if (s.joinCondition != null) {
                buff.append(" on ").append(s.joinCondition);
            }
        }
        
        // where ...
        if (where != null) {
            buff.append(" where ").append(where.toString());
        }
        
        // order by ...
        if (!orderList.isEmpty()) {
            buff.append(" order by ");
            for (int i = 0; i < orderList.size(); i++) {
                if (i > 0) {
                    buff.append(", ");
                }
                buff.append(orderList.get(i));
            }
        }
        // leave original xpath string as a comment
        appendXPathAsComment(buff, xpathQuery);
        return buff.toString();        
    }
    private static void appendXPathAsComment(StringBuilder buff, String xpath) {
        if (xpath == null) {
            return;
        }
        buff.append(" /* xpath: ");
        // the xpath query may contain the "end comment" marker
        String xpathEscaped = xpath.replaceAll("\\*\\/", "* /");
        buff.append(xpathEscaped);
        buff.append(" */");
    }
        public String toString() {
            StringBuilder buff = new StringBuilder();
            buff.append(s1).append(" union ").append(s2);
            // order by ...
            if (orderList != null && !orderList.isEmpty()) {
                buff.append(" order by ");
                for (int i = 0; i < orderList.size(); i++) {
                    if (i > 0) {
                        buff.append(", ");
                    }
                    buff.append(orderList.get(i));
                }
            }
            // leave original xpath string as a comment
            appendXPathAsComment(buff, xpathQuery);
            return buff.toString();
        }
