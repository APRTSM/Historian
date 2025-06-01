    NodeTree parseNode(JsopTokenizer t) throws Exception {
        NodeTree node = new NodeTree();
        if (!t.matches('}')) {
            do {
                String key = t.readString();
                t.read(':');
                if (t.matches('{')) {
                    node.nodes.put(key, parseNode(t));
                } else {
                    node.props.put(key, t.readRawValue().trim());
                }
            } while (t.matches(','));
            t.read('}');
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
    }
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
                    key("msg").value(commit.getMsg()).
                    key("changes").value(commit.getChanges()).endObject();
        }
        return commitBuff.endArray().toString();
    }
    protected AbstractCommit(Commit other) {
        this.parentId = other.getParentId();
        this.rootNodeId = other.getRootNodeId();
        this.msg = other.getMsg();
        this.changes = other.getChanges();
        this.commitTS = other.getCommitTS();
    }
    public void serialize(Binding binding) throws Exception {
        binding.write("rootNodeId", rootNodeId.getBytes());
        binding.write("commitTS", commitTS);
        binding.write("msg", msg == null ? "" : msg);
        binding.write("changes", changes == null ? "" : changes);
        binding.write("parentId", parentId == null ? "" : parentId.toString());
    }
    public String getChanges() {
        return changes;
    }
