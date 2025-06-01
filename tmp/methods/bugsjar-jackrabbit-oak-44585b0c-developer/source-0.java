    private Validator visibleValidator(@Nonnull Tree source,
                                       @Nonnull Tree dest) {
        // TODO improve: avoid calculating the 'before' permissions in case the current parent permissions already point to the correct tree.
        ImmutableTree parent = (ImmutableTree) moveCtx.rootBefore.getTree("/");
        TreePermission tp = getPermissionProvider().getTreePermission(parent, TreePermission.EMPTY);
        for (String n : PathUtils.elements(source.getPath())) {
            tp = tp.getChildPermission(n, parent.getChild(n).getNodeState());
        }
        Validator validator = createValidator(source, dest, tp, this);
        return new VisibleValidator(validator, true, false);
    }
