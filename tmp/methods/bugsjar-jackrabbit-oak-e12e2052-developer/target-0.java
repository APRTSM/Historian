        public boolean appliesTo(Tree state) {
            for (String mixinName : getMixinTypeNames(state)){
                if (nodeTypeName.equals(mixinName)){
                    return true;
                }
            }

            if (!nodeTypeName.equals(getPrimaryTypeName(state))) {
                return false;
            }
            //TODO Add support for condition
            //return condition == null || condition.evaluate(state);
            return true;
        }
    private static Iterable<String> getMixinTypeNames(Tree tree) {
        PropertyState property = tree.getProperty(JcrConstants.JCR_MIXINTYPES);
        return property != null ? property.getValue(Type.NAMES) : Collections.<String>emptyList();
    }
