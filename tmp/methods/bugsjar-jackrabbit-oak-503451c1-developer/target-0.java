    public void insert(NodeBuilder index, String key, boolean unique,
            Iterable<String> values) throws CommitFailedException {
        NodeBuilder child = index.child(key);
        if (unique
                && (child.getProperty("match") != null || child
                        .getChildNodeCount() > 0)) {
            throw new CommitFailedException(
                    "Uniqueness constraint violated for key " + key);
        }

        for (String add : values) {
            NodeBuilder indexEntry = child;
            for (String segment : PathUtils.elements(add)) {
                indexEntry = indexEntry.child(segment);
            }
            indexEntry.setProperty("match", true);
        }
    }
