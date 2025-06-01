    private Iterable<NodeDocument> readChildDocs(@Nonnull final String path,
                                                 @Nullable String name,
                                                 final int limit) {
        final String to = Utils.getKeyUpperLimit(checkNotNull(path));
        final String from;
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
        final StringValue key = new StringValue(path);
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
            String low = Utils.getIdFromPath(lastPath);
            int remainingLimit = limit - c.childNames.size();
            List<NodeDocument> docs = store.query(Collection.NODES,
                    low, to, remainingLimit);
            NodeDocument.Children clone = c.clone();
            for (NodeDocument doc : docs) {
                String p = doc.getPath();
                clone.childNames.add(PathUtils.getName(p));
            }
            clone.isComplete = docs.size() < remainingLimit;
            docChildrenCache.put(key, clone);
            c = clone;
        }
        Iterable<NodeDocument> head = filter(transform(c.childNames,
                new Function<String, NodeDocument>() {
            @Override
            public NodeDocument apply(String name) {
                String p = concat(path, name);
                NodeDocument doc = store.find(Collection.NODES, Utils.getIdFromPath(p));
                if (doc == null) {
                    docChildrenCache.invalidate(key);
                }
                return doc;
            }
        }), Predicates.notNull());
        Iterable<NodeDocument> it;
        if (c.isComplete) {
            it = head;
        } else {
            // OAK-2420: 'head' may have null documents when documents are
            // concurrently removed from the store. concat 'tail' to fetch
            // more documents if necessary
            final String last = getIdFromPath(concat(
                    path, c.childNames.get(c.childNames.size() - 1)));
            Iterable<NodeDocument> tail = new Iterable<NodeDocument>() {
                @Override
                public Iterator<NodeDocument> iterator() {
                    return store.query(NODES, last, to, limit).iterator();
                }
            };
            it = Iterables.concat(head, tail);
        }
        return Iterables.limit(it, limit);
    }
