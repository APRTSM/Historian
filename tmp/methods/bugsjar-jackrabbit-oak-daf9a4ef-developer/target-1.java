    private void purgePendingChanges() {
        branch.setRoot(rootTree.getNodeState());
        notifyListeners();
    }
    public void setRoot(NodeState newRoot) {
        if (!currentRoot.equals(newRoot)) {
            currentRoot = newRoot;
            commit(buildJsop());
        }
    }
