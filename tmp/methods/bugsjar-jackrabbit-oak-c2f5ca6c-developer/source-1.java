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
            String name = PathUtils.getName(c.children.get((int) i));
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
    private boolean dispatch(@Nonnull String jsonDiff,
                             @Nonnull DocumentNodeState base,
                             @Nonnull NodeStateDiff diff) {
        if (!AbstractNodeState.comparePropertiesAgainstBaseState(this, base, diff)) {
            return false;
        }
        if (jsonDiff.trim().isEmpty()) {
            return true;
        }
        JsopTokenizer t = new JsopTokenizer(jsonDiff);
        boolean continueComparison = true;
        while (continueComparison) {
            int r = t.read();
            if (r == JsopReader.END) {
                break;
            }
            switch (r) {
                case '+': {
                    String path = t.readString();
                    t.read(':');
                    t.read('{');
                    while (t.read() != '}') {
                        // skip properties
                    }
                    String name = PathUtils.getName(path);
                    continueComparison = diff.childNodeAdded(name, getChildNode(name));
                    break;
                }
                case '-': {
                    String path = t.readString();
                    String name = PathUtils.getName(path);
                    continueComparison = diff.childNodeDeleted(name, base.getChildNode(name));
                    break;
                }
                case '^': {
                    String path = t.readString();
                    t.read(':');
                    if (t.matches('{')) {
                        t.read('}');
                        String name = PathUtils.getName(path);
                        continueComparison = diff.childNodeChanged(name,
                                base.getChildNode(name), getChildNode(name));
                    } else if (t.matches('[')) {
                        // ignore multi valued property
                        while (t.read() != ']') {
                            // skip values
                        }
                    } else {
                        // ignore single valued property
                        t.read();
                    }
                    break;
                }
                case '>': {
                    String from = t.readString();
                    t.read(':');
                    String to = t.readString();
                    String fromName = PathUtils.getName(from);
                    continueComparison = diff.childNodeDeleted(
                            fromName, base.getChildNode(fromName));
                    if (!continueComparison) {
                        break;
                    }
                    String toName = PathUtils.getName(to);
                    continueComparison = diff.childNodeAdded(
                            toName, getChildNode(toName));
                    break;
                }
                default:
                    throw new IllegalArgumentException("jsonDiff: illegal token '"
                            + t.getToken() + "' at pos: " + t.getLastPos() + ' ' + jsonDiff);
            }
        }
        return continueComparison;
    }
