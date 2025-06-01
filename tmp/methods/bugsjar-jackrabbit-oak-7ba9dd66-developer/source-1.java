    private static void setSplitDocProps(NodeDocument mainDoc, NodeDocument oldDoc,
                                         UpdateOp old, Revision maxRev) {
        setSplitDocMaxRev(old, maxRev);

        SplitDocType type = SplitDocType.DEFAULT;
        if(!mainDoc.hasChildren()){
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
