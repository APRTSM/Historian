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
                for (ProtectedItemImporter pni : pItemImporters) {
                    if (pni instanceof ProtectedNodeImporter && ((ProtectedNodeImporter) pni).start(parent)) {
                        log.debug("Protected node -> delegated to ProtectedNodeImporter");
                        pnImporter = (ProtectedNodeImporter) pni;
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
                tree = createTree(parent, nodeInfo, id);
            } else {

                //1. First check from base state that tree corresponding to
                //this id exist
                Tree conflicting = baseStateIdManager.getTree(id);

                if(conflicting == null){
                    //1.a. Check if id is found in newly created nodes
                    if(uuids.contains(id)){
                        conflicting = currentStateIdManager.getTree(id);
                    }
                }else{
                    //1.b Re obtain the conflicting tree from Id Manager
                    //associated with current root. Such that any operation
                    //on it gets reflected in later operations
                    //In case a tree with same id was removed earlier then it
                    //would return null
                    conflicting = currentStateIdManager.getTree(id);
                }

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
        for (PropInfo pi : propInfos) {
            // find applicable definition
            //TODO find better heuristics?
            PropertyDefinition def = pi.getPropertyDef(effectiveNodeTypeProvider.getEffectiveNodeType(tree));

            if (def.isProtected()) {
                // skip protected property
                log.debug("Skipping protected property " + pi.getName());

                // notify the ProtectedPropertyImporter.
                for (ProtectedItemImporter ppi : pItemImporters) {
                    if (ppi instanceof ProtectedPropertyImporter
                            && ((ProtectedPropertyImporter) ppi).handlePropInfo(tree, pi, def)) {
                        log.debug("Protected property -> delegated to ProtectedPropertyImporter");
                        break;
                    } /* else: p-i-Importer isn't able to deal with this property.
                             try next pp-importer */

                }
            } else {
                // regular property -> create the property
                createProperty(tree, pi, def);
            }
        }

        parents.push(tree);
    }
