    Iterable<NodeDocument> readChildDocs(@Nonnull final String path,
                                         @Nullable String name,
                                         int limit) {
        String to = Utils.getKeyUpperLimit(checkNotNull(path));
        String from;
        if (name != null) {
            from = Utils.getIdFromPath(concat(path, name));
        } else {
            from = Utils.getKeyLowerLimit(path);
        }
        if (name != null || limit > NUM_CHILDREN_CACHE_LIMIT) {
            // do not use cache when there is a lower bound name
            // or more than 16k child docs are requested
            return store.query(Collection.NODES, from, to, limit);
        }
        StringValue key = new StringValue(path);
        // check cache
        NodeDocument.Children c = docChildrenCache.getIfPresent(key);
        if (c == null) {
            c = new NodeDocument.Children();
            List<NodeDocument> docs = store.query(Collection.NODES, from, to, limit);
            for (NodeDocument doc : docs) {
                String p = doc.getPath();
                c.childNames.add(PathUtils.getName(p));
            }
            c.isComplete = docs.size() < limit;
            docChildrenCache.put(key, c);
            return docs;
        } else if (c.childNames.size() < limit && !c.isComplete) {
            // fetch more and update cache
            String lastName = c.childNames.get(c.childNames.size() - 1);
            String lastPath = concat(path, lastName);
            from = Utils.getIdFromPath(lastPath);
            int remainingLimit = limit - c.childNames.size();
            List<NodeDocument> docs = store.query(Collection.NODES,
                    from, to, remainingLimit);
            NodeDocument.Children clone = c.clone();
            for (NodeDocument doc : docs) {
                String p = doc.getPath();
                clone.childNames.add(PathUtils.getName(p));
            }
            clone.isComplete = docs.size() < remainingLimit;
            docChildrenCache.put(key, clone);
            c = clone;
        }
        Iterable<NodeDocument> it = transform(c.childNames, new Function<String, NodeDocument>() {
            @Override
            public NodeDocument apply(String name) {
                String p = concat(path, name);
                NodeDocument doc = store.find(Collection.NODES, Utils.getIdFromPath(p));
                if (doc == null) {
                    docChildrenCache.invalidateAll();
                    throw new NullPointerException("Document " + p + " not found");
                }
                return doc;
            }
        });
        if (c.childNames.size() > limit * 2) {
            it = Iterables.limit(it, limit * 2);
        }
        return it;
    }
