    public CommitHookProvider getSecurityHooks() {
        return new CommitHookProvider() {
            @Override
            public CommitHook getCommitHook(String workspaceName) {
                return new CompositeHook(new PermissionHook(workspaceName), new VersionablePathHook(workspaceName));
            }
        };
    }
