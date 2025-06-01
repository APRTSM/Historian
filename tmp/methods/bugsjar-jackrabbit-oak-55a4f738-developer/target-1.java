    public CommitHookProvider getSecurityHooks() {
        return new CommitHookProvider() {
            @Override
            public CommitHook getCommitHook(String workspaceName) {
                //FIXME return new CompositeHook(new PermissionHook(workspaceName), new VersionablePathHook(workspaceName));
                return new CompositeHook(new VersionablePathHook(workspaceName));
            }
        };
    }
    public PrivilegeBits getBits(@Nonnull String... privilegeNames) {
        if (privilegeNames.length == 0) {
            return PrivilegeBits.EMPTY;
        }

        Tree privilegesTree = getPrivilegesTree();
        if (privilegesTree == null) {
            return PrivilegeBits.EMPTY;
        }
        PrivilegeBits bits = PrivilegeBits.getInstance();
        for (String privilegeName : privilegeNames) {
            Tree defTree = privilegesTree.getChild(checkNotNull(privilegeName));
            if (defTree != null) {
                bits.add(PrivilegeBits.getInstance(defTree));
            }
        }
        return bits.unmodifiable();
    }
