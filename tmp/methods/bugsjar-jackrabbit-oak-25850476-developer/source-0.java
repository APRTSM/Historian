    private void disconnect(NodeDocument splitDoc) {
        String splitId = splitDoc.getId();
        String mainId = Utils.getIdFromPath(splitDoc.getMainPath());
        NodeDocument doc = store.find(NODES, mainId);
        if (doc == null) {
            LOG.warn("Main document {} already removed. Split document is {}",
                    mainId, splitId);
            return;
        }
        int slashIdx = splitId.lastIndexOf('/');
        int height = Integer.parseInt(splitId.substring(slashIdx + 1));
        Revision rev = Revision.fromString(
                splitId.substring(splitId.lastIndexOf('/', slashIdx - 1) + 1, slashIdx));
        doc = doc.findPrevReferencingDoc(rev, height);
        if (doc == null) {
            LOG.warn("Split document {} not referenced anymore. Main document is {}",
                    splitId, mainId);
            return;
        }
        // remove reference
        if (doc.getSplitDocType() == INTERMEDIATE) {
            disconnectFromIntermediate(doc, rev);
        } else {
            markStaleOnMain(doc, rev, height);
        }
    }
