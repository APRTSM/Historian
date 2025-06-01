    void toJson(JsopBuilder builder, NodeState node,
                int depth, int offset, int maxChildNodes,
                boolean inclVirtualProps, NodeFilter filter) {
        for (PropertyState property : node.getProperties()) {
            if (filter == null || filter.includeProperty(property.getName())) {
                builder.key(property.getName()).encodedValue(property.getEncodedValue());
            }
        }
        long childCount = node.getChildNodeCount();
        if (inclVirtualProps) {
            if (filter == null || filter.includeProperty(":childNodeCount")) {
                // :childNodeCount is by default always included
                // unless it is explicitly excluded in the filter
                builder.key(":childNodeCount").value(childCount);
            }
            // check whether :hash has been explicitly included
            if (filter != null) {
                NameFilter nf = filter.getPropertyFilter();
                if (nf != null
                        && nf.getInclusionPatterns().contains(":hash")
                        && !nf.getExclusionPatterns().contains(":hash")) {
                    builder.key(":hash").value(rep.getRevisionStore().getId(node).toString());
                }
            }
        }
        if (childCount > 0 && depth >= 0) {
            if (filter != null) {
                NameFilter childFilter = filter.getChildNodeFilter();
                if (childFilter != null && !childFilter.containsWildcard()) {
                    // optimization for large child node lists:
                    // no need to iterate over the entire child node list if the filter
                    // does not include wildcards
                    int count = maxChildNodes == -1 ? Integer.MAX_VALUE : maxChildNodes;
                    for (String name : childFilter.getInclusionPatterns()) {
                        NodeState child = node.getChildNode(name);
                        if (child != null) {
                            boolean incl = true;
                            for (String exclName : childFilter.getExclusionPatterns()) {
                                if (name.equals(exclName)) {
                                    incl = false;
                                    break;
                                }
                            }
                            if (incl) {
                                if (count-- <= 0) {
                                    break;
                                }
                                builder.key(name).object();
                                if (depth > 0) {
                                    toJson(builder, child, depth - 1, 0, maxChildNodes, inclVirtualProps, filter);
                                }
                                builder.endObject();
                            }
                        }
                    }
                    return;
                }
            }

            int count = maxChildNodes;
            if (count != -1
                    && filter != null
                    && filter.getChildNodeFilter() != null) {
                // specific maxChildNodes limit and child node filter
                count = -1;
            }
            int numSiblings = 0;
            for (ChildNode entry : node.getChildNodeEntries(offset, count)) {
                if (filter == null || filter.includeNode(entry.getName())) {
                    if (maxChildNodes != -1 && ++numSiblings > maxChildNodes) {
                        break;
                    }
                    builder.key(entry.getName()).object();
                    if (depth > 0) {
                        toJson(builder, entry.getNode(), depth - 1, 0, maxChildNodes, inclVirtualProps, filter);
                    }
                    builder.endObject();
                }
            }
        }
    }
