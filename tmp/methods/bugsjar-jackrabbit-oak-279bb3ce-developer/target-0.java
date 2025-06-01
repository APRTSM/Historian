    public String getCommitRootPath(Revision revision) {
        String depth = getCommitRootDepth(revision);
        if (depth != null) {
            if (depth.equals("0")) {
                return "/";
            }
            String p = Utils.getPathFromId(getId());
            return PathUtils.getAncestorPath(p,
                    PathUtils.getDepth(p) - Integer.parseInt(depth));
        }
        return null;
    }
    private String getCommitRootDepth(@Nonnull Revision revision) {
        // check local map first
        Map<Revision, String> local = getLocalCommitRoot();
        String depth = local.get(revision);
        if (depth == null) {
            // check previous
            for (NodeDocument prev : getPreviousDocs(COMMIT_ROOT, revision)) {
                depth = prev.getCommitRootDepth(revision);
                if (depth != null) {
                    break;
                }
            }
        }
        return depth;
    }
