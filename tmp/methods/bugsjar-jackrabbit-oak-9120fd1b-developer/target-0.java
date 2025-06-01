    public boolean compareAgainstBaseState(NodeState base, NodeStateDiff diff) {
        if (this == base || fastEquals(this, base)) {
             return true; // no changes
        } else if (base == EMPTY_NODE || !base.exists()) { // special case
            return EmptyNodeState.compareAgainstEmptyState(this, diff);
        } else if (!(base instanceof SegmentNodeState)) { // fallback
            return AbstractNodeState.compareAgainstBaseState(this, base, diff);
        }

        SegmentNodeState that = (SegmentNodeState) base;
        if (that.wasCompactedTo(this)) {
            return true; // no changes during compaction
        }

        Template beforeTemplate = that.getTemplate();
        RecordId beforeId = that.getRecordId();

        Template afterTemplate = getTemplate();
        RecordId afterId = getRecordId();

        // Compare type properties
        if (!compareProperties(
                beforeTemplate.getPrimaryType(), afterTemplate.getPrimaryType(),
                diff)) {
            return false;
        }
        if (!compareProperties(
                beforeTemplate.getMixinTypes(), afterTemplate.getMixinTypes(),
                diff)) {
            return false;
        }

        // Compare other properties, leveraging the ordering
        int beforeIndex = 0;
        int afterIndex = 0;
        PropertyTemplate[] beforeProperties =
                beforeTemplate.getPropertyTemplates();
        PropertyTemplate[] afterProperties =
                afterTemplate.getPropertyTemplates();
        while (beforeIndex < beforeProperties.length
                && afterIndex < afterProperties.length) {
            int d = Integer.valueOf(afterProperties[afterIndex].hashCode())
                    .compareTo(Integer.valueOf(beforeProperties[beforeIndex].hashCode()));
            if (d == 0) {
                d = afterProperties[afterIndex].getName().compareTo(
                        beforeProperties[beforeIndex].getName());
            }
            PropertyState beforeProperty = null;
            PropertyState afterProperty = null;
            if (d < 0) {
                afterProperty =
                        afterTemplate.getProperty(afterId, afterIndex++);
            } else if (d > 0) {
                beforeProperty =
                        beforeTemplate.getProperty(beforeId, beforeIndex++);
            } else {
                afterProperty =
                        afterTemplate.getProperty(afterId, afterIndex++);
                beforeProperty =
                        beforeTemplate.getProperty(beforeId, beforeIndex++);
            }
            if (!compareProperties(beforeProperty, afterProperty, diff)) {
                return false;
            }
        }
        while (afterIndex < afterProperties.length) {
            if (!diff.propertyAdded(
                    afterTemplate.getProperty(afterId, afterIndex++))) {
                return false;
            }
        }
        while (beforeIndex < beforeProperties.length) {
            PropertyState beforeProperty =
                    beforeTemplate.getProperty(beforeId, beforeIndex++);
            if (!diff.propertyDeleted(beforeProperty)) {
                return false;
            }
        }

        String beforeChildName = beforeTemplate.getChildName();
        String afterChildName = afterTemplate.getChildName();
        if (afterChildName == Template.ZERO_CHILD_NODES) {
            if (beforeChildName != Template.ZERO_CHILD_NODES) {
                for (ChildNodeEntry entry
                        : beforeTemplate.getChildNodeEntries(beforeId)) {
                    if (!diff.childNodeDeleted(
                            entry.getName(), entry.getNodeState())) {
                        return false;
                    }
                }
            }
        } else if (afterChildName != Template.MANY_CHILD_NODES) {
            NodeState afterNode =
                    afterTemplate.getChildNode(afterChildName, afterId);
            NodeState beforeNode =
                    beforeTemplate.getChildNode(afterChildName, beforeId);
            if (!beforeNode.exists()) {
                if (!diff.childNodeAdded(afterChildName, afterNode)) {
                    return false;
                }
            } else if (!fastEquals(afterNode, beforeNode)) {
                if (!diff.childNodeChanged(
                        afterChildName, beforeNode, afterNode)) {
                    return false;
                }
            }
            if (beforeChildName == Template.MANY_CHILD_NODES
                    || (beforeChildName != Template.ZERO_CHILD_NODES
                        && !beforeNode.exists())) {
                for (ChildNodeEntry entry
                        : beforeTemplate.getChildNodeEntries(beforeId)) {
                    if (!afterChildName.equals(entry.getName())) {
                        if (!diff.childNodeDeleted(
                                entry.getName(), entry.getNodeState())) {
                            return false;
                        }
                    }
                }
            }
        } else if (beforeChildName == Template.ZERO_CHILD_NODES) {
            for (ChildNodeEntry entry
                    : afterTemplate.getChildNodeEntries(afterId)) {
                if (!diff.childNodeAdded(
                        entry.getName(), entry.getNodeState())) {
                    return false;
                }
            }
        } else if (beforeChildName != Template.MANY_CHILD_NODES) {
            boolean beforeChildStillExists = false;
            for (ChildNodeEntry entry
                    : afterTemplate.getChildNodeEntries(afterId)) {
                String childName = entry.getName();
                beforeChildStillExists |= childName.equals(beforeChildName);
                NodeState afterChild = entry.getNodeState();
                if (beforeChildName.equals(childName)) {
                    NodeState beforeChild =
                            beforeTemplate.getChildNode(beforeChildName, beforeId);
                    if (beforeChild.exists()) {
                        if (!fastEquals(afterChild, beforeChild)
                                && !diff.childNodeChanged(
                                        childName, beforeChild, afterChild)) {
                            return false;
                        }
                    } else {
                        if (!diff.childNodeAdded(childName, afterChild)) {
                            return false;
                        }
                    }
                } else if (!diff.childNodeAdded(childName, afterChild)) {
                    return false;
                }
            }
            if (!beforeChildStillExists) {
                NodeState beforeChild =
                        beforeTemplate.getChildNode(beforeChildName, beforeId);
                if (!diff.childNodeDeleted(beforeChildName, beforeChild)) {
                    return false;
                }
            }
        } else {
            MapRecord afterMap = afterTemplate.getChildNodeMap(afterId);
            MapRecord beforeMap = beforeTemplate.getChildNodeMap(beforeId);
            return afterMap.compare(beforeMap, diff);
        }

        return true;
    }
