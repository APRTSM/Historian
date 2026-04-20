    public String getCommitRootPath(Revision revision) {
        // check local map first
        Map<Revision, String> local = getLocalCommitRoot();
        String depth = local.get(revision);
        if (depth != null) {
            if (depth.equals("0")) {
                return "/";
            }
            String p = Utils.getPathFromId(getId());
            return PathUtils.getAncestorPath(p,
                    PathUtils.getDepth(p) - Integer.parseInt(depth));
        }
        // check previous
        for (NodeDocument prev : getPreviousDocs(COMMIT_ROOT, revision)) {
            String path = prev.getCommitRootPath(revision);
            if (path != null) {
                return path;
            }
        }
        return null;
    }
