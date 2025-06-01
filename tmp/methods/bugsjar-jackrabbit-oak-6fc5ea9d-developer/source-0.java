    public TreeLocation getChild(String name) {
        T child = getChildTree(name);
        if (child != null) {
            return createNodeLocation(child);
        }

        PropertyState prop = getPropertyState(name);
        if (prop != null) {
            return createPropertyLocation(this, name);
        }
        return new NullLocation(this, name);
    }
