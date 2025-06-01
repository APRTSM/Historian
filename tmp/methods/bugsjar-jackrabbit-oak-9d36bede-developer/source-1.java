    private void collectDeletedDocuments(VersionGCStats stats, Revision headRevision, long oldestRevTimeStamp) {
        List<String> docIdsToDelete = new ArrayList<String>();
        Iterable<NodeDocument> itr = versionStore.getPossiblyDeletedDocs(oldestRevTimeStamp);
        try {
            for (NodeDocument doc : itr) {
                //Check if node is actually deleted at current revision
                //As node is not modified since oldestRevTimeStamp then
                //this node has not be revived again in past maxRevisionAge
                //So deleting it is safe
                if (doc.getNodeAtRevision(nodeStore, headRevision, null) == null) {
                    docIdsToDelete.add(doc.getId());
                    //Collect id of all previous docs also
                    for (NodeDocument prevDoc : ImmutableList.copyOf(doc.getAllPreviousDocs())) {
                        docIdsToDelete.add(prevDoc.getId());
                    }
                }
            }
        } finally {
            Utils.closeIfCloseable(itr);
        }

        if(log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("Deleted document with following ids were deleted as part of GC \n");
            Joiner.on(StandardSystemProperty.LINE_SEPARATOR.value()).appendTo(sb, docIdsToDelete);
            log.debug(sb.toString());
        }
        nodeStore.getDocumentStore().remove(Collection.NODES, docIdsToDelete);
        stats.deletedDocGCCount += docIdsToDelete.size();
    }
