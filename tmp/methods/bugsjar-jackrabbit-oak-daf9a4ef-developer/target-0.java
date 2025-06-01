    private void purgePendingChanges() {
        branch.setRoot(rootTree.getNodeState());
        notifyListeners();
    }
