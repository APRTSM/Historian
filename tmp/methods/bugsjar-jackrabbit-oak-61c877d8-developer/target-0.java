    public TreePermission getTreePermission(@Nonnull ImmutableTree tree, @Nonnull TreePermission parentPermission) {
        if (tree.isRoot()) {
            return new TreePermissionImpl(tree, TreeTypeProvider.TYPE_DEFAULT, TreePermission.EMPTY);
        }
        int type = tree.getType();
        switch (type) {
            case TreeTypeProvider.TYPE_HIDDEN:
                // TODO: OAK-753 decide on where to filter out hidden items.
                return TreePermission.ALL;
            case TreeTypeProvider.TYPE_VERSION:
                String ntName = TreeUtil.getPrimaryTypeName(tree);
                if (ntName == null) {
                    return TreePermission.EMPTY;
                }
                if (VersionConstants.VERSION_STORE_NT_NAMES.contains(ntName) || VersionConstants.NT_ACTIVITY.equals(ntName)) {
                    return new TreePermissionImpl(tree, TreeTypeProvider.TYPE_VERSION, parentPermission);
                } else {
                    ImmutableTree versionableTree = getVersionableTree(tree);
                    if (versionableTree == null) {
                        log.warn("Cannot retrieve versionable node for " + tree.getPath());
                        return TreePermission.EMPTY;
                    } else {
                        // TODO: may return wrong results in case of restrictions
                        // TODO that would match the path of the versionable node
                        // TODO (or item in the subtree) but that item no longer exists
                        // TODO -> evaluation by path would be more accurate (-> see #isGranted)
                        while (!versionableTree.exists()) {
                            versionableTree = versionableTree.getParent();
                        }
                        TreePermission pp = getParentPermission(versionableTree, TreeTypeProvider.TYPE_VERSION);
                        return new TreePermissionImpl(versionableTree, TreeTypeProvider.TYPE_VERSION, pp);
                    }
                }
            case TreeTypeProvider.TYPE_PERMISSION_STORE:
                return TreePermission.EMPTY;
            default:
                return new TreePermissionImpl(tree, type, parentPermission);
        }
    }
