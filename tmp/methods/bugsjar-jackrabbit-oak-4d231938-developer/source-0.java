    public int getType(Tree tree) {
        if (tree.isRoot()) {
            return TYPE_DEFAULT;
        } else {
            return getType(tree, getType(tree.getParent()));
        }
    }
    public TreeTypeProvider(@Nonnull Context contextInfo) {
        this.contextInfo = contextInfo;
    }
    public int getType(Tree tree, int parentType) {
            if (tree.isRoot()) {
                return TYPE_DEFAULT;
            }

            int type;
            switch (parentType) {
                case TYPE_HIDDEN:
                    type = TYPE_HIDDEN;
                    break;
                case TYPE_VERSION:
                    type = TYPE_VERSION;
                    break;
                case TYPE_INTERNAL:
                    type = TYPE_INTERNAL;
                    break;
                case TYPE_AC:
                    type = TYPE_AC;
                    break;
                default:
                    String name = tree.getName();
                    if (NodeStateUtils.isHidden(name)) {
                        type = TYPE_HIDDEN;
                    } else if (VersionConstants.VERSION_STORE_ROOT_NAMES.contains(name)) {
                        type = TYPE_VERSION;
                    } else if (PermissionConstants.REP_PERMISSION_STORE.equals(name)) {
                        type = TYPE_INTERNAL;
                    } else if (contextInfo.definesContextRoot(tree)) {
                        type = TYPE_AC;
                    } else {
                        type = TYPE_DEFAULT;
                    }
            }
            return type;
        }
