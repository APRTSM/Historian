    public String getNodes(String path, String revisionId, int depth,
            long offset, int maxChildNodes, String filter)
            throws MicroKernelException {
        if (depth != 0) {
            throw new MicroKernelException("Only depth 0 is supported, depth is " + depth);
        }
        revisionId = revisionId != null ? revisionId : nodeStore.getHeadRevision().toString();
        Revision rev = Revision.fromString(revisionId);
        DocumentNodeState n = nodeStore.getNode(path, rev);
        if (n == null) {
            return null;
        }
        JsopStream json = new JsopStream();
        boolean includeId = filter != null && filter.contains(":id");
        includeId |= filter != null && filter.contains(":hash");
        json.object();
        n.append(json, includeId);
        int max;
        if (maxChildNodes == -1) {
            max = Integer.MAX_VALUE;
            maxChildNodes = Integer.MAX_VALUE;
        } else {
            // use long to avoid overflows
            long m = ((long) maxChildNodes) + offset;
            max = (int) Math.min(m, Integer.MAX_VALUE);
        }
        Children c = nodeStore.getChildren(n, null, max);
        for (long i = offset; i < c.children.size(); i++) {
            if (maxChildNodes-- <= 0) {
                break;
            }
            String name = c.children.get((int) i);
            json.key(name).object().endObject();
        }
        if (c.hasMore) {
            // TODO use a better way to notify there are more children
            json.key(":childNodeCount").value(Long.MAX_VALUE);
        } else {
            json.key(":childNodeCount").value(c.children.size());
        }
        json.endObject();
        return json.toString();
    }
