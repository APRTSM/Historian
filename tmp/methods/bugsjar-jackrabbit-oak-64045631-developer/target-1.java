    NodeState merge(CommitHook hook, CommitInfo info) throws CommitFailedException {
        purge();
        boolean success = false;
        try {
            branch.merge(hook, info);
            success = true;
        } finally {
            if (!success) {
                // need to adjust base and head of this builder
                // in case branch.merge() did a rebase and then
                // a commit hook failed the merge
                super.reset(branch.getHead());
                this.base = branch.getBase();
            }
        }
        return reset();
    }
    NodeState merge(CommitHook hook, CommitInfo info) throws CommitFailedException {
        purge();
        boolean success = false;
        try {
            branch.merge(hook, info);
            success = true;
        } finally {
            if (!success) {
                // need to adjust base and head of this builder
                // in case branch.merge() did a rebase and then
                // a commit hook failed the merge
                super.reset(branch.getHead());
                this.base = branch.getBase();
            }
        }
        return reset();
    }
