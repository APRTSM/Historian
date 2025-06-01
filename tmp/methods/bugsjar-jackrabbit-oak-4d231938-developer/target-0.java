    public int getType(@Nonnull Tree tree, int parentType) {
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
                type = getType(tree.getName(), tree);
        }
        return type;
    }
    private int getType(@Nonnull String name, @Nonnull Tree tree) {
        int type;
        if (NodeStateUtils.isHidden(name)) {
            type = TYPE_HIDDEN;
        } else if (VersionConstants.VERSION_STORE_ROOT_NAMES.contains(name)) {
            type = (JcrConstants.JCR_SYSTEM.equals(tree.getParent().getName())) ?  TYPE_VERSION : TYPE_DEFAULT;
        } else if (PermissionConstants.REP_PERMISSION_STORE.equals(name)) {
            type = TYPE_INTERNAL;
        } else if (authorizationContext.definesContextRoot(tree)) {
            type = TYPE_AC;
        } else {
            type = TYPE_DEFAULT;
        }
        return type;
    }
    public int getType(@Nonnull Tree tree) {
        if (tree.isRoot()) {
            return TYPE_DEFAULT;
        } else {
            Tree t = tree;
            while (!t.isRoot()) {
                int type = getType(t.getName(), t);
                // stop walking up the hierarchy as soon as a special type is found
                if (TYPE_DEFAULT != type) {
                    return type;
                }
                t = t.getParent();
            }
            return TYPE_DEFAULT;
        }
    }
    public TreeTypeProvider(@Nonnull Context authorizationContext) {
        this.authorizationContext = authorizationContext;
    }
