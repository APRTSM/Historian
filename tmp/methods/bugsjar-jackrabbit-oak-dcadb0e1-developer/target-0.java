    public void endNode(NodeInfo nodeInfo) throws RepositoryException {
        Tree parent = parents.pop();
        if (parent == null) {
            if (pnImporter != null) {
                pnImporter.endChildInfo();
            }
        } else if (getDefinition(parent).isProtected()) {
            if (pnImporter != null) {
                pnImporter.end(parent);
                // and reset the pnImporter field waiting for the next protected
                // parent -> selecting again from available importers
                pnImporter = null;
            }
        }

        idLookup.rememberImportedUUIDs(parent);
    }
        private IdResolver(@Nonnull Root root, @Nonnull ContentSession contentSession) {
            currentStateIdManager = new IdentifierManager(root);
            baseStateIdManager = new IdentifierManager(contentSession.getLatestRoot());

            if (!root.hasPendingChanges()) {
                importedUUIDs = new HashSet<String>();
            } else {
                importedUUIDs = null;
            }
        }
        private Tree getConflictingTree(@Nonnull String id) {
            //1. First check from base state that tree corresponding to
            //this id exist
            Tree conflicting = baseStateIdManager.getTree(id);
            if (conflicting == null && importedUUIDs != null) {
                //1.a. Check if id is found in newly created nodes
                if (importedUUIDs.contains(id)) {
                    conflicting = currentStateIdManager.getTree(id);
                }
            } else {
                //1.b Re obtain the conflicting tree from Id Manager
                //associated with current root. Such that any operation
                //on it gets reflected in later operations
                //In case a tree with same id was removed earlier then it
                //would return null
                conflicting = currentStateIdManager.getTree(id);
            }
            return conflicting;
        }
    public void startNode(NodeInfo nodeInfo, List<PropInfo> propInfos)
            throws RepositoryException {
        Tree parent = parents.peek();
        Tree tree = null;
        String id = nodeInfo.getUUID();
        String nodeName = nodeInfo.getName();
        String ntName = nodeInfo.getPrimaryTypeName();

        if (parent == null) {
            log.debug("Skipping node: " + nodeName);
            // parent node was skipped, skip this child node too
            parents.push(null); // push null onto stack for skipped node
            // notify the p-i-importer
            if (pnImporter != null) {
                pnImporter.startChildInfo(nodeInfo, propInfos);
            }
            return;
        }

        NodeDefinition parentDef = getDefinition(parent);
        if (parentDef.isProtected()) {
            // skip protected node
            parents.push(null);
            log.debug("Skipping protected node: " + nodeName);

            if (pnImporter != null) {
                // pnImporter was already started (current nodeInfo is a sibling)
                // notify it about this child node.
                pnImporter.startChildInfo(nodeInfo, propInfos);
            } else {
                // no importer defined yet:
                // test if there is a ProtectedNodeImporter among the configured
                // importers that can handle this.
                // if there is one, notify the ProtectedNodeImporter about the
                // start of a item tree that is protected by this parent. If it
                // potentially is able to deal with it, notify it about the child node.
                for (ProtectedNodeImporter pni : getNodeImporters()) {
                    if (pni.start(parent)) {
                        log.debug("Protected node -> delegated to ProtectedNodeImporter");
                        pnImporter = pni;
                        pnImporter.startChildInfo(nodeInfo, propInfos);
                        break;
                    } /* else: p-i-Importer isn't able to deal with the protected tree.
                     try next. and if none can handle the passed parent the
                     tree below will be skipped */
                }
            }
            return;
        }

        if (parent.hasChild(nodeName)) {
            // a node with that name already exists...
            Tree existing = parent.getChild(nodeName);
            NodeDefinition def = getDefinition(existing);
            if (!def.allowsSameNameSiblings()) {
                // existing doesn't allow same-name siblings,
                // check for potential conflicts
                if (def.isProtected() && isNodeType(existing, ntName)) {
                    /*
                     use the existing node as parent for the possible subsequent
                     import of a protected tree, that the protected node importer
                     may or may not be able to deal with.
                     -> upon the next 'startNode' the check for the parent being
                        protected will notify the protected node importer.
                     -> if the importer is able to deal with that node it needs
                        to care of the complete subtree until it is notified
                        during the 'endNode' call.
                     -> if the import can't deal with that node or if that node
                        is the a leaf in the tree to be imported 'end' will
                        not have an effect on the importer, that was never started.
                    */
                    log.debug("Skipping protected node: " + existing);
                    parents.push(existing);
                    /**
                     * let ProtectedPropertyImporters handle the properties
                     * associated with the imported node. this may include overwriting,
                     * merging or just adding missing properties.
                     */
                    importProperties(existing, propInfos, true);
                    return;
                }
                if (def.isAutoCreated() && isNodeType(existing, ntName)) {
                    // this node has already been auto-created, no need to create it
                    tree = existing;
                } else {
                    // edge case: colliding node does have same uuid
                    // (see http://issues.apache.org/jira/browse/JCR-1128)
                    String existingIdentifier = IdentifierManager.getIdentifier(existing);
                    if (!(existingIdentifier.equals(id)
                            && (uuidBehavior == ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING
                            || uuidBehavior == ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING))) {
                        throw new ItemExistsException(
                                "Node with the same UUID exists:" + existing);
                    }
                    // fall through
                }
            }
        }

        if (tree == null) {
            // create node
            if (id == null) {
                // no potential uuid conflict, always add new node
                tree = createTree(parent, nodeInfo, null);
            } else if (uuidBehavior == ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW) {
                // always create a new UUID even if no
                // conflicting node exists. see OAK-1244
                tree = createTree(parent, nodeInfo, UUID.randomUUID().toString());
                // remember uuid mapping
                if (isNodeType(tree, JcrConstants.MIX_REFERENCEABLE)) {
                    refTracker.put(nodeInfo.getUUID(), TreeUtil.getString(tree, JcrConstants.JCR_UUID));
                }
            } else {

                Tree conflicting = idLookup.getConflictingTree(id);
                if (conflicting != null && conflicting.exists()) {
                    // resolve uuid conflict
                    tree = resolveUUIDConflict(parent, conflicting, id, nodeInfo);
                    if (tree == null) {
                        // no new node has been created, so skip this node
                        parents.push(null); // push null onto stack for skipped node
                        log.debug("Skipping existing node " + nodeInfo.getName());
                        return;
                    }
                } else {
                    // create new with given uuid
                    tree = createTree(parent, nodeInfo, id);
                }
            }
        }

        // process properties
        importProperties(tree, propInfos, false);

        parents.push(tree);
    }
        private void rememberImportedUUIDs(@CheckForNull Tree tree) {
            if (tree == null || importedUUIDs == null) {
                return;
            }

            String uuid = TreeUtil.getString(tree, JcrConstants.JCR_UUID);
            if (uuid != null) {
                importedUUIDs.add(uuid);
            }

            for (Tree child : tree.getChildren()) {
                rememberImportedUUIDs(child);
            }
        }
    public ImporterImpl(String absPath,
                        SessionContext sessionContext,
                        Root root,
                        int uuidBehavior,
                        boolean isWorkspaceImport) throws RepositoryException {
        String oakPath = sessionContext.getOakPath(absPath);
        if (oakPath == null) {
            throw new RepositoryException("Invalid name or path: " + absPath);
        }
        if (!PathUtils.isAbsolute(oakPath)) {
            throw new RepositoryException("Not an absolute path: " + absPath);
        }

        SessionDelegate sd = sessionContext.getSessionDelegate();
        if (isWorkspaceImport && sd.hasPendingChanges()) {
            throw new RepositoryException("Pending changes on session. Cannot run workspace import.");
        }

        this.uuidBehavior = uuidBehavior;
        userID = sd.getAuthInfo().getUserID();

        importTargetTree = root.getTree(oakPath);
        if (!importTargetTree.exists()) {
            throw new PathNotFoundException(absPath);
        }

        WorkspaceImpl wsp = sessionContext.getWorkspace();
        VersionManager vMgr = wsp.getVersionManager();
        if (!vMgr.isCheckedOut(absPath)) {
            throw new VersionException("Target node is checked in.");
        }
        if (importTargetTree.getStatus() != Tree.Status.NEW && wsp.getLockManager().isLocked(absPath)) {
            throw new LockException("Target node is locked.");
        }
        effectiveNodeTypeProvider = wsp.getNodeTypeManager();
        definitionProvider = wsp.getNodeTypeManager();
        ntTypesRoot = root.getTree(NODE_TYPES_PATH);

        accessManager = sessionContext.getAccessManager();

        idLookup = new IdResolver(root, sd.getContentSession());

        refTracker = new ReferenceChangeTracker();

        parents = new Stack<Tree>();
        parents.push(importTargetTree);

        pItemImporters.clear();
        for (ProtectedItemImporter importer : sessionContext.getProtectedItemImporters()) {
            // FIXME this passes the session scoped name path mapper also for workspace imports
            if (importer.init(sessionContext.getSession(), root, sessionContext, isWorkspaceImport, uuidBehavior, refTracker, sessionContext.getSecurityProvider())) {
                pItemImporters.add(importer);
            }
        }
    }
