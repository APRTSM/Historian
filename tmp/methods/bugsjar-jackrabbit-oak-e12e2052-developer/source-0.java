        public boolean appliesTo(Tree state) {
            if (!nodeTypeName.equals(getPrimaryTypeName(state))) {
                return false;
            }
            //TODO Add support for condition
            //return condition == null || condition.evaluate(state);
            return true;
        }
    private static Iterable<String> getMixinTypeNames(Tree tree) {
        PropertyState property = tree.getProperty(JcrConstants.JCR_MIMETYPE);
        return property != null ? property.getValue(Type.NAMES) : Collections.<String>emptyList();
    }
