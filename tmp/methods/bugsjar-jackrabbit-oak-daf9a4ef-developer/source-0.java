    private void purgePendingChanges() {
        if (hasPendingChanges()) {
            branch.setRoot(rootTree.getNodeState());
        }
        notifyListeners();
    }
