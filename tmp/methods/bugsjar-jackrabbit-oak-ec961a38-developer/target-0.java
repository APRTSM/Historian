    public void remove(NodeBuilder index, String key, Iterable<String> values) {
        if (!index.hasChildNode(key)) {
            return;
        }
        NodeBuilder child = index.child(key);
        Map<String, NodeBuilder> parents = new TreeMap<String, NodeBuilder>(Collections.reverseOrder());

        for (String rm : values) {
            if (PathUtils.denotesRoot(rm)) {
                child.removeProperty("match");
            } else {
                String parentPath = PathUtils.getParentPath(rm);
                String name = PathUtils.getName(rm);
                NodeBuilder indexEntry = parents.get(parentPath);
                if (indexEntry == null) {
                    indexEntry = child;
                    String segmentPath = "";
                    Iterator<String> segments = PathUtils.elements(parentPath)
                            .iterator();
                    while (segments.hasNext()) {
                        String segment = segments.next();
                        segmentPath = PathUtils.concat(segmentPath, segment);
                        indexEntry = indexEntry.child(segment);
                        parents.put(segmentPath, indexEntry);
                    }
                }
                if (indexEntry.hasChildNode(name)) {
                    NodeBuilder childEntry = indexEntry.child(name);
                    childEntry.removeProperty("match");
                    if (childEntry.getChildNodeCount() == 0) {
                        indexEntry.removeNode(name);
                    }
                }
            }
        }
        // prune the index: remove all children that have no children
        // and no "match" property progressing bottom up
        Iterator<String> it = parents.keySet().iterator();
        while (it.hasNext()) {
            String path = it.next();
            NodeBuilder parent = parents.get(path);
            pruneNode(parent);
        }

        // finally prune the index node
        pruneNode(child);
        if (child.getChildNodeCount() == 0
                && child.getProperty("match") == null) {
            index.removeNode(key);
        }
    }
    public void insert(NodeBuilder index, String key, boolean unique,
            Iterable<String> values) throws CommitFailedException {
        NodeBuilder child = index.child(key);

        for (String add : values) {
            NodeBuilder indexEntry = child;
            for(String segment: PathUtils.elements(add)){
                indexEntry = indexEntry.child(segment);
            }
            indexEntry.setProperty("match", true);
        }
        long matchCount = countMatchingLeaves(child.getNodeState());
        if (matchCount == 0) {
            index.removeNode(key);
        } else if (unique && matchCount > 1) {
            throw new CommitFailedException("Uniqueness constraint violated");
        }
    }
    private void pruneNode(NodeBuilder parent) {
        if (parent.isRemoved()) {
            return;
        }
        for (String name : parent.getChildNodeNames()) {
            NodeBuilder segment = parent.child(name);
            if (segment.getChildNodeCount() == 0
                    && segment.getProperty("match") == null) {
                parent.removeNode(name);
            }
        }
    }
