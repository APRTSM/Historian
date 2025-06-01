    private boolean canEvalPathRestrictions(IndexingRule rule) {
        if (filter.getPathRestriction() == Filter.PathRestriction.NO_RESTRICTION){
            return false;
        }
        //If no other restrictions is provided and query is pure
        //path restriction based then need to be sure that index definition at least
        //allows indexing all the path for given nodeType
        return definition.evaluatePathRestrictions() && rule.indexesAllNodesOfMatchingType();
    }
