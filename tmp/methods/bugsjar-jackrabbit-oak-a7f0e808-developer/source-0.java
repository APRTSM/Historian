        public void childNodeAdded(String name, NodeState after) {
            childNodeChanged(name, EMPTY_NODE, after);
        }
        public void childNodeDeleted(String name, NodeState before) {
            childNodeChanged(name, before, EMPTY_NODE);
        }
