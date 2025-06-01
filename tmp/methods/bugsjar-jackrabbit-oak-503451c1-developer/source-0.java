    public void insert(NodeBuilder index, String key, boolean unique,
            Iterable<String> values) throws CommitFailedException {
        NodeBuilder child = index.child(key);

        for (String add : values) {
            NodeBuilder indexEntry = child;
            for (String segment : PathUtils.elements(add)) {
                indexEntry = indexEntry.child(segment);
            }
            indexEntry.setProperty("match", true);
        }
        CountingNodeVisitor v = new CountingNodeVisitor(2);
        v.visit(child.getNodeState());
        int matchCount = v.getCount();
        if (matchCount == 0) {
            index.removeNode(key);
        } else if (unique && matchCount > 1) {
            throw new CommitFailedException("Uniqueness constraint violated for key " + key);
        }
    }
