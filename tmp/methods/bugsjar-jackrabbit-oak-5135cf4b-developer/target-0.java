        public boolean childNodeChanged(
                String name, NodeState before, NodeState after) {
            if (NodeStateUtils.isHidden(name)) {
                // stop comparison
                return false;
            }
            Node node = new Node(nodeAfter, name);
            return after.compareAgainstBaseState(
                    before, new Diff(versionManager, node, exceptions));
        }
        private boolean setVersionablePath(PropertyState after) {
            if (JcrConstants.JCR_VERSIONHISTORY.equals(after.getName()) && nodeAfter.isVersionable(versionManager)) {
                NodeBuilder vhBuilder;
                try {
                    vhBuilder = versionManager.getOrCreateVersionHistory(
                            nodeAfter.builder, Collections.<String, Object>emptyMap());
                } catch (CommitFailedException e) {
                    exceptions.add(e);
                    // stop further comparison
                    return false;
                }

                if (!vhBuilder.hasProperty(JcrConstants.JCR_MIXINTYPES)) {
                    vhBuilder.setProperty(
                            JcrConstants.JCR_MIXINTYPES,
                            ImmutableSet.of(MIX_REP_VERSIONABLE_PATHS),
                            Type.NAMES);
                }

                String versionablePath = nodeAfter.path;
                vhBuilder.setProperty(workspaceName, versionablePath, Type.PATH);
            }
            return true;
        }
