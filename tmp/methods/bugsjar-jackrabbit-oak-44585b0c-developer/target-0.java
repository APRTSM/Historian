    private Validator visibleValidator(@Nonnull Tree source,
                                       @Nonnull Tree dest) {
        // TODO improve: avoid calculating the 'before' permissions in case the current parent permissions already point to the correct tree.
        ImmutableTree immutableTree = (ImmutableTree) moveCtx.rootBefore.getTree("/");
        TreePermission tp = getPermissionProvider().getTreePermission(immutableTree, TreePermission.EMPTY);
        for (String n : PathUtils.elements(source.getPath())) {
            immutableTree = immutableTree.getChild(n);
            tp = tp.getChildPermission(n, immutableTree.getNodeState());
        }
        Validator validator = createValidator(source, dest, tp, this);
        return new VisibleValidator(validator, true, false);
    }
