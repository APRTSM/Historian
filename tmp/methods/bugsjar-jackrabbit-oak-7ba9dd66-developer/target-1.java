    private static boolean referencesOldDocAfterSplit(NodeDocument mainDoc,
                                                      NodeDocument oldDoc) {
        Set<Revision> revs = oldDoc.getLocalRevisions().keySet();
        for (String property : mainDoc.data.keySet()) {
            if (IGNORE_ON_SPLIT.contains(property)) {
                continue;
            }
            Set<Revision> changes = Sets.newHashSet(mainDoc.getLocalMap(property).keySet());
            changes.removeAll(oldDoc.getLocalMap(property).keySet());
            if (!disjoint(changes, revs)) {
                return true;
            }
        }
        return false;
    }
    private static void setSplitDocProps(NodeDocument mainDoc, NodeDocument oldDoc,
                                         UpdateOp old, Revision maxRev) {
        setSplitDocMaxRev(old, maxRev);

        SplitDocType type = SplitDocType.DEFAULT;
        if(!mainDoc.hasChildren() && !referencesOldDocAfterSplit(mainDoc, oldDoc)){
            type = SplitDocType.DEFAULT_NO_CHILD;
        } else if (oldDoc.getLocalRevisions().isEmpty()){
            type = SplitDocType.PROP_COMMIT_ONLY;
        }

        //Copy over the hasBinary flag
        if(mainDoc.hasBinary()){
            setHasBinary(old);
        }

        setSplitDocType(old,type);
    }
