    public TreeLocation getChild(String name) {
        PropertyState prop = getPropertyState(name);
        if (prop != null) {
            return createPropertyLocation(this, name);
        }

        T child = getChildTree(name);
        if (child != null) {
            return createNodeLocation(child);
        }
        return new NullLocation(this, name);
    }
