    private void rollback(List<UpdateOp> newDocuments,
                          List<UpdateOp> changed,
                          UpdateOp commitRoot) {
        DocumentStore store = nodeStore.getDocumentStore();
        for (UpdateOp op : changed) {
            UpdateOp reverse = op.getReverseOperation();
            store.findAndUpdate(NODES, reverse);
        }
        for (UpdateOp op : newDocuments) {
            UpdateOp reverse = op.getReverseOperation();
            store.findAndUpdate(NODES, reverse);
        }
        UpdateOp removeCollision = new UpdateOp(commitRoot.getId(), false);
        NodeDocument.removeCollision(removeCollision, revision);
        store.findAndUpdate(NODES, removeCollision);
    }
