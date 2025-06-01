    public long count(NodeState indexMeta, Set<String> values, int max) {
        NodeState index = indexMeta.getChildNode(INDEX_CONTENT_NODE_NAME);
        int count = 0;
        if (values == null) {
            PropertyState ec = indexMeta.getProperty(ENTRY_COUNT_PROPERTY_NAME);
            if (ec != null) {
                return ec.getValue(Type.LONG);
            }
            CountingNodeVisitor v = new CountingNodeVisitor(max);
            v.visit(index);
            count = v.getEstimatedCount();
            // "is not null" queries typically read more data
            count *= 10;
        } else {
            int size = values.size();
            if (size == 0) {
                return 0;
            }
            max = Math.max(10, max / size);
            int i = 0;
            for (String p : values) {
                if (count > max && i > 3) {
                    count = count / size / i;
                    break;
                }
                NodeState s = index.getChildNode(p);
                if (s.exists()) {
                    CountingNodeVisitor v = new CountingNodeVisitor(max);
                    v.visit(s);
                    count += v.getEstimatedCount();
                }
                i++;
            }
        }
        return count;
    }
