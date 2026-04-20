    private void rollback(List<UpdateOp> newDocuments,
                          List<UpdateOp> changed,
                          UpdateOp commitRoot) {
        DocumentStore store = nodeStore.getDocumentStore();
        for (UpdateOp op : changed) {
            UpdateOp reverse = op.getReverseOperation();
            if (op.isNew()) {
                NodeDocument.setDeletedOnce(reverse);
            }
            store.findAndUpdate(NODES, reverse);
        }
        for (UpdateOp op : newDocuments) {
            UpdateOp reverse = op.getReverseOperation();
            NodeDocument.setDeletedOnce(reverse);
            store.findAndUpdate(NODES, reverse);
        }
        UpdateOp removeCollision = new UpdateOp(commitRoot.getId(), false);
        NodeDocument.removeCollision(removeCollision, revision);
        store.findAndUpdate(NODES, removeCollision);
    }
