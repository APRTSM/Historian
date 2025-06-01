    public String getJournal(String fromRevision, String toRevision, String filter) throws MicroKernelException {
        if (rep == null) {
            throw new IllegalStateException("this instance has already been disposed");
        }

        Id fromRevisionId = Id.fromString(fromRevision);
        Id toRevisionId = toRevision == null ? getHeadRevisionId() : Id.fromString(toRevision);

        List<StoredCommit> commits = new ArrayList<StoredCommit>();
        try {
            StoredCommit toCommit = rep.getCommit(toRevisionId);

            Commit fromCommit;
            if (toRevisionId.equals(fromRevisionId)) {
                fromCommit = toCommit;
            } else {
                fromCommit = rep.getCommit(fromRevisionId);
                if (fromCommit.getCommitTS() > toCommit.getCommitTS()) {
                    // negative range, return empty array
                    return "[]";
                }
            }

            // collect commits, starting with toRevisionId
            // and traversing parent commit links until we've reached
            // fromRevisionId
            StoredCommit commit = toCommit;
            while (commit != null) {
                commits.add(commit);
                if (commit.getId().equals(fromRevisionId)) {
                    break;
                }
                Id commitId = commit.getParentId();
                if (commitId == null) {
                    break;
                }
                commit = rep.getCommit(commitId);
            }
        } catch (Exception e) {
            throw new MicroKernelException(e);
        }

        JsopBuilder commitBuff = new JsopBuilder().array();
        // iterate over commits in chronological order,
        // starting with oldest commit
        for (int i = commits.size() - 1; i >= 0; i--) {
            StoredCommit commit = commits.get(i);
            if (commit.getParentId() == null) {
                continue;
            }
            commitBuff.object().
                    key("id").value(commit.getId().toString()).
                    key("ts").value(commit.getCommitTS()).
                    key("msg").value(commit.getMsg());
            String diff = diffCache.get(commit.getId());
            if (diff == null) {
                diff = diff(commit.getParentId(), commit.getId(), filter);
                diffCache.put(commit.getId(), diff);
            }
            commitBuff.key("changes").value(diff).endObject();
        }
        return commitBuff.endArray().toString();
    }
    public void dispose() {
        gate.commit("end");
        if (rep != null) {
            try {
                rep.shutDown();
            } catch (Exception ignore) {
                // fail silently
            }
            rep = null;
        }
        diffCache.clear();
    }
    static void addNode(LinkedList<AddNodeOperation> list, String path, String name, JsopTokenizer t) throws Exception {
        AddNodeOperation op = new AddNodeOperation();
        op.path = path;
        op.name = name;
        list.add(op);
        if (!t.matches('}')) {
            do {
                String key = t.readString();
                t.read(':');
                if (t.matches('{')) {
                    addNode(list, PathUtils.concat(path, name), key, t);
                } else {
                    op.props.put(key, t.readRawValue().trim());
                }
            } while (t.matches(','));
            t.read('}');
        }
    protected AbstractCommit(Commit other) {
        this.parentId = other.getParentId();
        this.rootNodeId = other.getRootNodeId();
        this.msg = other.getMsg();
        this.commitTS = other.getCommitTS();
    }
