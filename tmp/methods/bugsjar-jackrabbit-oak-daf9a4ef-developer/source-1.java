    private void purgePendingChanges() {
        if (hasPendingChanges()) {
            branch.setRoot(rootTree.getNodeState());
        }
        notifyListeners();
    }
    public void setRoot(NodeState newRoot) {
        currentRoot = newRoot;
        commit(buildJsop());
    }
