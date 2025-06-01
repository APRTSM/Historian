    public boolean definesContextRoot(@Nonnull Tree tree) {
        String name = tree.getName();
        if (POLICY_NODE_NAMES.contains(name)) {
            return NT_REP_ACL.equals(TreeUtil.getPrimaryTypeName(tree));
        } else {
            return REP_PERMISSION_STORE.equals(name);
        }
    }
