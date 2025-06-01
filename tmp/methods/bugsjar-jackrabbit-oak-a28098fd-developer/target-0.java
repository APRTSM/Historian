    public ItemDelegate getItem(String path) {
        String name = PathUtils.getName(path);
        if (name.isEmpty()) {
            return getRootNode();
        } else {
            Tree parent = root.getTree(PathUtils.getParentPath(path));

            Tree child = parent.getChild(name);
            if (child.exists()) {
                return new NodeDelegate(this, child);
            } else if (parent.hasProperty(name)) {
                return new PropertyDelegate(this, parent, name);
            } else {
                return null;
            }
        }
    }
