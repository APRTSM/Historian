    public Tree getTree() throws InvalidItemStateException {
        if (!tree.exists()) {
            throw new InvalidItemStateException("Item is stale");
        }
        return tree;
    }
    public Iterator<NodeDelegate> getChildren() throws InvalidItemStateException {
        Iterator<Tree> iterator = getTree().getChildren().iterator();
        return transform(
                filter(iterator, new Predicate<Tree>() {
                    @Override
                    public boolean apply(Tree tree) {
                        return !tree.getName().startsWith(":");
                    }
                }),
                new Function<Tree, NodeDelegate>() {
                    @Override
                    public NodeDelegate apply(Tree tree) {
                        return new NodeDelegate(sessionDelegate, tree);
                    }
                });
    }
