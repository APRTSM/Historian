    public NodeState getChildNode(@Nonnull String name) {
        if (!hasChildren) {
            checkValidName(name);
            return EmptyNodeState.MISSING_NODE;
        }
        String p = PathUtils.concat(getPath(), name);
        DocumentNodeState child = store.getNode(p, lastRevision);
        if (child == null) {
            checkValidName(name);
            return EmptyNodeState.MISSING_NODE;
        } else {
            return child;
        }
    }
    private boolean dispatch(@Nonnull String jsonDiff,
                             @Nonnull DocumentNodeState node,
                             @Nonnull DocumentNodeState base,
                             @Nonnull NodeStateDiff diff,
                             boolean useReadRevision) {
        if (jsonDiff.trim().isEmpty()) {
            return true;
        }
        Revision nodeRev = useReadRevision ? node.getRevision() : node.getLastRevision();
        Revision baseRev = useReadRevision ? base.getRevision() : base.getLastRevision();
        JsopTokenizer t = new JsopTokenizer(jsonDiff);
        boolean continueComparison = true;
        while (continueComparison) {
            int r = t.read();
            if (r == JsopReader.END) {
                break;
            }
            switch (r) {
                case '+': {
                    String name = unshareString(t.readString());
                    t.read(':');
                    t.read('{');
                    while (t.read() != '}') {
                        // skip properties
                    }
                    NodeState child = getNode(concat(node.getPath(), name), nodeRev);
                    continueComparison = diff.childNodeAdded(name, child);
                    break;
                }
                case '-': {
                    String name = unshareString(t.readString());
                    NodeState child = getNode(concat(base.getPath(), name), baseRev);
                    continueComparison = diff.childNodeDeleted(name, child);
                    break;
                }
                case '^': {
                    String name = unshareString(t.readString());
                    t.read(':');
                    if (t.matches('{')) {
                        t.read('}');
                        NodeState nodeChild = getNode(concat(node.getPath(), name), nodeRev);
                        NodeState baseChild = getNode(concat(base.getPath(), name), baseRev);
                        continueComparison = diff.childNodeChanged(
                                name, baseChild, nodeChild);
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
                default:
                    throw new IllegalArgumentException("jsonDiff: illegal token '"
                            + t.getToken() + "' at pos: " + t.getLastPos() + ' ' + jsonDiff);
            }
        }
        return continueComparison;
    }
