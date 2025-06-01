    public void insert(NodeBuilder index, String key, boolean unique,
            Iterable<String> values) throws CommitFailedException {
        NodeBuilder child = index.child(key);

        for (String add : values) {
            NodeBuilder indexEntry = child;
            Iterator<String> segments = PathUtils.elements(add).iterator();
            while (segments.hasNext()) {
                String segment = segments.next();
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
    public void remove(NodeBuilder index, String key, Iterable<String> values) {
        if (!index.hasChildNode(key)) {
            return;
        }
        NodeBuilder child = index.child(key);
        Queue<NodeBuilder> parentQueue = new LinkedList<NodeBuilder>();
        for (String rm : values) {
            if (PathUtils.denotesRoot(rm)) {
                child.removeProperty("match");
            } else {
                NodeBuilder indexEntry = child;
                Iterator<String> segments = PathUtils.elements(rm).iterator();
                while (segments.hasNext()) {
                    String segment = segments.next();
                    if (segments.hasNext()) {
                        parentQueue.add(indexEntry);
                        indexEntry = indexEntry.child(segment);
                    } else {
                        // last segment
                        if (indexEntry.hasChildNode(segment)) {
                            indexEntry.removeNode(segment);
                        }
                    }
                }
            }
        }
        // prune the index: remove all children that have no children
        // and no "match" property progressing bottom up
        // see OAK-520
        // while (!parentQueue.isEmpty()) {
        // NodeBuilder node = parentQueue.poll();
        // for (String name : node.getChildNodeNames()) {
        // NodeBuilder segment = node.child(name);
        // if (segment.getChildNodeCount() == 0
        // && segment.getProperty("match") == null) {
        // segment.removeNode(name);
        // }
        // }
        // }
        // finally remove the index node if empty
        if (child.getChildNodeCount() == 0) {
            index.removeNode(key);
        }
    }
