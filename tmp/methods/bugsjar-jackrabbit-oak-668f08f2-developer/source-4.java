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
        void apply() throws Exception {
            setProperty(nodePath, propName, propValue);
        }
    public void copyNode(String srcPath, String destPath) throws NotFoundException, Exception {
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

        // update change log
        changeLog.add(new CopyNode(srcPath, destPath));
    }
        void apply() throws Exception {
            moveNode(srcPath, destPath);
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
            // copy log in order to avoid concurrent modifications
            List<Change> log = new ArrayList<Change>(changeLog);
            for (Change change : log) {
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
            newCommit.setRootNodeId(rootNodeId);
            newRevId = store.putHeadCommit(newCommit);
        } finally {
            store.unlockHead();
        }

        // reset instance in order to be reusable
        staged.clear();
        changeLog.clear();

        return newRevId;
    }
        void apply() throws Exception {
            removeNode(nodePath);
        }
    public void moveNode(String srcPath, String destPath) throws NotFoundException, Exception {
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

        // update change log
        changeLog.add(new MoveNode(srcPath, destPath));
    }
        void apply() throws Exception {
            addNode(parentNodePath, nodeName, properties);
        }
    public void addNode(String parentNodePath, String nodeName, Map<String, String> properties) throws Exception {
        MutableNode modParent = getOrCreateStagedNode(parentNodePath);
        if (modParent.getChildNodeEntry(nodeName) != null) {
            throw new Exception("there's already a child node with name '" + nodeName + "'");
        }
        String newPath = PathUtils.concat(parentNodePath, nodeName);
        MutableNode newChild = new MutableNode(store, newPath);
        newChild.getProperties().putAll(properties);

        // id will be computed on commit
        modParent.add(new ChildNode(nodeName, null));
        staged.put(newPath, newChild);
        // update change log
        changeLog.add(new AddNode(parentNodePath, nodeName, properties));
    }
        void apply() throws Exception {
            setProperties(nodePath, properties);
        }
    public void addNode(String parentNodePath, String nodeName) throws Exception {
        addNode(parentNodePath, nodeName, Collections.<String, String>emptyMap());
    }
        void apply() throws Exception {
            copyNode(srcPath, destPath);
        }
        AddNode(String parentNodePath, String nodeName, Map<String, String> properties) {
            this.parentNodePath = parentNodePath;
            this.nodeName = nodeName;
            this.properties = properties;
        }
    public void setProperties(String nodePath, Map<String, String> properties) throws Exception {
        MutableNode node = getOrCreateStagedNode(nodePath);

        node.getProperties().clear();
        node.getProperties().putAll(properties);

        // update change log
        changeLog.add(new SetProperties(nodePath, properties));
    }
    public void setProperty(String nodePath, String propName, String propValue) throws Exception {
        MutableNode node = getOrCreateStagedNode(nodePath);

        Map<String, String> properties = node.getProperties();
        if (propValue == null) {
            properties.remove(propName);
        } else {
            properties.put(propName, propValue);
        }

        // update change log
        changeLog.add(new SetProperty(nodePath, propName, propValue));
    }
        SetProperties(String nodePath, Map<String, String> properties) {
            this.nodePath = nodePath;
            this.properties = properties;
        }
    public void removeNode(String nodePath) throws NotFoundException, Exception {
        String parentPath = PathUtils.getParentPath(nodePath);
        String nodeName = PathUtils.getName(nodePath);

        MutableNode parent = getOrCreateStagedNode(parentPath);
        if (parent.remove(nodeName) == null) {
            throw new NotFoundException(nodePath);
        }

        // update staging area
        removeStagedNodes(nodePath);

        // update change log
        changeLog.add(new RemoveNode(nodePath));
    }
    public MutableCommit(StoredCommit other) {
        setParentId(other.getParentId());
        setRootNodeId(other.getRootNodeId());
        setCommitTS(other.getCommitTS());
        setMsg(other.getMsg());
        this.id = other.getId();
    }
