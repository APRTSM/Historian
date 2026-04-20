        public void childNodeDeleted(String name, NodeState before) {
            if (NodeStateUtils.isHidden(name)) {
                return;
            }
            if (exception == null) {
                try {
                    Validator v = validator.childNodeDeleted(name, before);
                    if (v != null) {
                        validate(v, before, EMPTY_NODE);
                    }
                } catch (CommitFailedException e) {
                    exception = e;
                }
            }
        }
        public void childNodeAdded(String name, NodeState after) {
            if (NodeStateUtils.isHidden(name)) {
                return;
            }
            if (exception == null) {
                try {
                    Validator v = validator.childNodeAdded(name, after);
                    if (v != null) {
                        validate(v, EMPTY_NODE, after);
                    }
                } catch (CommitFailedException e) {
                    exception = e;
                }
            }
        }
