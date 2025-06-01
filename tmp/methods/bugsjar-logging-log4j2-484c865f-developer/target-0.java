    private Node constructNode(final String name, final Node parent, final JsonNode jsonNode) {
        final PluginType<?> type = pluginManager.getPluginType(name);
        final Node node = new Node(parent, name, type);
        processAttributes(node, jsonNode);
        final Iterator<Map.Entry<String, JsonNode>> iter = jsonNode.fields();
        final List<Node> children = node.getChildren();
        while (iter.hasNext()) {
            final Map.Entry<String, JsonNode> entry = iter.next();
            final JsonNode n = entry.getValue();
            if (n.isArray() || n.isObject()) {
                if (type == null) {
                    status.add(new Status(name, n, ErrorType.CLASS_NOT_FOUND));
                }
                if (n.isArray()) {
                    LOGGER.debug("Processing node for array " + entry.getKey());
                    for (int i = 0; i < n.size(); ++i) {
                        final String pluginType = getType(n.get(i), entry.getKey());
                        final PluginType<?> entryType = pluginManager.getPluginType(pluginType);
                        final Node item = new Node(node, entry.getKey(), entryType);
                        processAttributes(item, n.get(i));
                        if (pluginType.equals(entry.getKey())) {
                            LOGGER.debug("Processing " + entry.getKey() + "[" + i + "]");
                        } else {
                            LOGGER.debug("Processing " + pluginType + " " + entry.getKey() + "[" + i + "]");
                        }
                        final Iterator<Map.Entry<String, JsonNode>> itemIter = n.get(i).fields();
                        final List<Node> itemChildren = item.getChildren();
                        while (itemIter.hasNext()) {
                            final Map.Entry<String, JsonNode> itemEntry = itemIter.next();
                            if (itemEntry.getValue().isObject()) {
                                LOGGER.debug("Processing node for object " + itemEntry.getKey());
                                itemChildren.add(constructNode(itemEntry.getKey(), item, itemEntry.getValue()));
                            } else if (itemEntry.getValue().isArray()) {
                                JsonNode array = itemEntry.getValue();
                                String entryName = itemEntry.getKey();
                                LOGGER.debug("Processing array for object " + entryName);
                                final PluginType<?> itemEntryType = pluginManager.getPluginType(entryName);
                                for (int j = 0; j < array.size(); ++j) {
                                    itemChildren.add(constructNode(entryName, item, array.get(j)));
                                }
                            }

                        }
                        children.add(item);
                    }
                } else {
                    LOGGER.debug("Processing node for object " + entry.getKey());
                    children.add(constructNode(entry.getKey(), node, n));
                }
            } else {
                LOGGER.debug("Node {} is of type {}", entry.getKey(), n.getNodeType());
            }
        }

        String t;
        if (type == null) {
            t = "null";
        } else {
            t = type.getElementName() + ":" + type.getPluginClass();
        }

        final String p = node.getParent() == null ? "null" : node.getParent().getName() == null ?
            "root" : node.getParent().getName();
        LOGGER.debug("Returning " + node.getName() + " with parent " + p + " of type " +  t);
        return node;
    }
