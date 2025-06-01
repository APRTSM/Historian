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
    public String getChanges();
    public void copyNode(String srcPath, String destPath) throws NotFoundException, Exception {
        Change change = new CopyNode(srcPath, destPath);
        change.apply();
        // update change log
        changeLog.add(change);
    }
        void apply() throws Exception {
            if (PathUtils.isAncestor(srcPath, destPath)) {
                throw new Exception("target path cannot be descendant of source path: " + destPath);
            }

            String srcParentPath = PathUtils.getParentPath(srcPath);
            String srcNodeName = PathUtils.getName(srcPath);

            String destParentPath = PathUtils.getParentPath(destPath);
            String destNodeName = PathUtils.getName(destPath);

            MutableNode srcParent = getOrCreateStagedNode(srcParentPath);
            if (srcParentPath.equals(destParentPath)) {
                if (srcParent.getChildNodeEntry(destNodeName) != null) {
                    throw new Exception("node already exists at move destination path: " + destPath);
                }
                if (srcParent.rename(srcNodeName, destNodeName) == null) {
                    throw new NotFoundException(srcPath);
                }
            } else {
                ChildNode srcCNE = srcParent.remove(srcNodeName);
                if (srcCNE == null) {
                    throw new NotFoundException(srcPath);
                }

                MutableNode destParent = getOrCreateStagedNode(destParentPath);
                if (destParent.getChildNodeEntry(destNodeName) != null) {
                    throw new Exception("node already exists at move destination path: " + destPath);
                }
                destParent.add(new ChildNode(destNodeName, srcCNE.getId()));
            }

            // update staging area
            moveStagedNodes(srcPath, destPath);
        }
    public void removeNode(String nodePath) throws NotFoundException, Exception {
        Change change = new RemoveNode(nodePath);
        change.apply();
        // update change log
        changeLog.add(change);
    }
        AddNode(String parentNodePath, String nodeName, NodeTree node) {
            this.parentNodePath = parentNodePath;
            this.nodeName = nodeName;
            this.node = node;
        }
    public Id /* new revId */ doCommit() throws Exception {
        if (staged.isEmpty()) {
            // nothing to commit
            return baseRevId;
        }

        Id currentHead = store.getHeadCommitId();
        if (!currentHead.equals(baseRevId)) {
            // todo gracefully handle certain conflicts (e.g. changes on moved sub-trees, competing deletes etc)
            // update base revision to new head
            baseRevId = currentHead;
            // clear staging area
            staged.clear();
            // replay change log on new base revision
            for (Change change : changeLog) {
                change.apply();
            }
        }

        Id rootNodeId = persistStagedNodes();

        Id newRevId;
        store.lockHead();
        try {
            currentHead = store.getHeadCommitId();
            if (!currentHead.equals(baseRevId)) {
                StoredNode baseRoot = store.getRootNode(baseRevId);
                StoredNode theirRoot = store.getRootNode(currentHead);
                StoredNode ourRoot = store.getNode(rootNodeId);

                rootNodeId = mergeTree(baseRoot, ourRoot, theirRoot);

                baseRevId = currentHead;
            }

            if (store.getCommit(currentHead).getRootNodeId().equals(rootNodeId)) {
                // the commit didn't cause any changes,
                // no need to create new commit object/update head revision
                return currentHead;
            }
            MutableCommit newCommit = new MutableCommit();
            newCommit.setParentId(baseRevId);
            newCommit.setCommitTS(System.currentTimeMillis());
            newCommit.setMsg(msg);
            StringBuilder diff = new StringBuilder();
            for (Change change : changeLog) {
                if (diff.length() > 0) {
                    diff.append('\n');
                }
                diff.append(change.asDiff());
            }
            newCommit.setChanges(diff.toString());
            newCommit.setRootNodeId(rootNodeId);
            newRevId = store.putHeadCommit(newCommit);
        } finally {
            store.unlockHead();
        }

        // reset instance
        staged.clear();
        changeLog.clear();

        return newRevId;
    }
        String asDiff() {
            StringBuffer diff = new StringBuffer("*");
            diff.append('"').append(srcPath).append("\":\"").append(destPath).append('"');
            return diff.toString();
        }
        String asDiff() {
            StringBuffer diff = new StringBuffer("-");
            diff.append('"').append(nodePath).append('"');
            return diff.toString();
        }
        private void recursiveAddNode(String parentPath, String name, NodeTree node) throws Exception {
            MutableNode modParent = getOrCreateStagedNode(parentPath);
            if (modParent.getChildNodeEntry(name) != null) {
                throw new Exception("there's already a child node with name '" + name + "'");
            }
            String newPath = PathUtils.concat(parentPath, name);
            MutableNode newChild = new MutableNode(store, newPath);
            newChild.getProperties().putAll(node.props);

            // id will be computed on commit
            modParent.add(new ChildNode(name, null));
            staged.put(newPath, newChild);

            for (String childName : node.nodes.keySet()) {
                recursiveAddNode(PathUtils.concat(parentPath, name), childName, node.nodes.get(childName));
            }
        }
        void apply() throws Exception {
            recursiveAddNode(parentNodePath, nodeName, node);
        }
        String asDiff() {
            StringBuffer diff = new StringBuffer("+");
            diff.append('"').append(PathUtils.concat(parentNodePath, nodeName)).append("\":");
            node.toJson(diff);
            return diff.toString();
        }
    public void addNode(String parentNodePath, String nodeName, NodeTree node) throws Exception {
        Change change = new AddNode(parentNodePath, nodeName, node);
        change.apply();
        // update change log
        changeLog.add(change);
    }
    public void setProperty(String nodePath, String propName, String propValue) throws Exception {
        Change change = new SetProperty(nodePath, propName, propValue);
        change.apply();
        // update change log
        changeLog.add(change);
    }
    public void moveNode(String srcPath, String destPath) throws NotFoundException, Exception {
        Change change = new MoveNode(srcPath, destPath);
        change.apply();
        // update change log
        changeLog.add(change);
    }
        void apply() throws Exception {
            MutableNode node = getOrCreateStagedNode(nodePath);

            Map<String, String> properties = node.getProperties();
            if (propValue == null) {
                properties.remove(propName);
            } else {
                properties.put(propName, propValue);
            }
        }
        void toJson(StringBuffer buf) {
            toJson(buf, this);
        }
        void apply() throws Exception {
            String srcParentPath = PathUtils.getParentPath(srcPath);
            String srcNodeName = PathUtils.getName(srcPath);

            String destParentPath = PathUtils.getParentPath(destPath);
            String destNodeName = PathUtils.getName(destPath);

            MutableNode srcParent = getOrCreateStagedNode(srcParentPath);
            ChildNode srcCNE = srcParent.getChildNodeEntry(srcNodeName);
            if (srcCNE == null) {
                throw new NotFoundException(srcPath);
            }

            MutableNode destParent = getOrCreateStagedNode(destParentPath);
            destParent.add(new ChildNode(destNodeName, srcCNE.getId()));

            if (srcCNE.getId() == null) {
                // a 'new' node is being copied

                // update staging area
                copyStagedNodes(srcPath, destPath);
            }
        }
        abstract String asDiff();
    }

    class AddNode extends Change {
        String asDiff() {
            StringBuffer diff = new StringBuffer("^");
            diff.append('"').append(PathUtils.concat(nodePath, propName)).append("\":").append(propValue);
            return diff.toString();
        }
        void apply() throws Exception {
            String parentPath = PathUtils.getParentPath(nodePath);
            String nodeName = PathUtils.getName(nodePath);

            MutableNode parent = getOrCreateStagedNode(parentPath);
            if (parent.remove(nodeName) == null) {
                throw new NotFoundException(nodePath);
            }

            // update staging area
            removeStagedNodes(nodePath);
        }
        String asDiff() {
            StringBuffer diff = new StringBuffer(">");
            diff.append('"').append(srcPath).append("\":\"").append(destPath).append('"');
            return diff.toString();
        }
    public void setChanges(String changes) {
        this.changes = changes;
    }
    public MutableCommit(StoredCommit other) {
        setParentId(other.getParentId());
        setRootNodeId(other.getRootNodeId());
        setCommitTS(other.getCommitTS());
        setMsg(other.getMsg());
        setChanges(other.getChanges());
        this.id = other.getId();
    }
    public StoredCommit(Id id, Id parentId, long commitTS, Id rootNodeId, String msg, String changes) {
        this.id = id;
        this.parentId = parentId;
        this.commitTS = commitTS;
        this.rootNodeId = rootNodeId;
        this.msg = msg;
        this.changes = changes;
    }
    public static StoredCommit deserialize(Id id, Binding binding) throws Exception {
        Id rootNodeId = new Id(binding.readBytesValue("rootNodeId"));
        long commitTS = binding.readLongValue("commitTS");
        String msg = binding.readStringValue("msg");
        String changes = binding.readStringValue("changes");
        String parentId = binding.readStringValue("parentId");
        return new StoredCommit(id, "".equals(parentId) ? null : Id.fromString(parentId),
                commitTS, rootNodeId, "".equals(msg) ? null : msg, changes);
    }
