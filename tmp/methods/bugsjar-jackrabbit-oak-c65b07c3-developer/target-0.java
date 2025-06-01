    private boolean canEvalPathRestrictions(IndexingRule rule) {
        //Opt out if one is looking for all children for '/' as its equivalent to
        //NO_RESTRICTION
        if (filter.getPathRestriction() == Filter.PathRestriction.NO_RESTRICTION
                || (filter.getPathRestriction() == Filter.PathRestriction.ALL_CHILDREN
                        && PathUtils.denotesRoot(filter.getPath()))
                ){
            return false;
        }
        //If no other restrictions is provided and query is pure
        //path restriction based then need to be sure that index definition at least
        //allows indexing all the path for given nodeType
        return definition.evaluatePathRestrictions() && rule.indexesAllNodesOfMatchingType();
    }
