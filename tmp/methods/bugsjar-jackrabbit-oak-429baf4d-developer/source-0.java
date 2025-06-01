    public boolean definesContextRoot(@Nonnull Tree tree) {
        String name = tree.getName();
        return POLICY_NODE_NAMES.contains(name) || REP_PERMISSION_STORE.equals(name);
    }
