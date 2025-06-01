        public PropertyDefinition getConfig(String propertyName) {
            PropertyDefinition config = propConfigs.get(propertyName.toLowerCase(Locale.ENGLISH));
            if (config != null) {
                return config;
            } else if (namePatterns.size() > 0) {
                // check patterns
                for (NamePattern np : namePatterns) {
                    if (np.matches(propertyName)) {
                        return np.getConfig();
                    }
                }
            }
            return null;
        }
        private Map<String, PropertyDefinition> collectPropConfigs(NodeState config, List<NamePattern> patterns,
                                                                   List<Aggregate.Include> propAggregate,
                                                                   List<PropertyDefinition> nonExistentProperties) {
            Map<String, PropertyDefinition> propDefns = newHashMap();
            NodeState propNode = config.getChildNode(LuceneIndexConstants.PROP_NODE);

            if (!propNode.exists()){
                return Collections.emptyMap();
            }

            if (!hasOrderableChildren(propNode)){
                log.warn("Properties node for [{}] does not have orderable " +
                "children in [{}]", this, IndexDefinition.this);
            }

            //Include all immediate child nodes to 'properties' node by default
            Tree propTree = TreeFactory.createReadOnlyTree(propNode);
            for (Tree prop : propTree.getChildren()) {
                String propName = prop.getName();
                NodeState propDefnNode = propNode.getChildNode(propName);
                if (propDefnNode.exists() && !propDefns.containsKey(propName)) {
                    PropertyDefinition pd = new PropertyDefinition(this, propName, propDefnNode);
                    if(pd.isRegexp){
                        patterns.add(new NamePattern(pd.name, pd));
                    } else {
                        propDefns.put(pd.name.toLowerCase(Locale.ENGLISH), pd);
                    }

                    if (pd.relative){
                        propAggregate.add(new Aggregate.PropertyInclude(pd));
                    }

                    if (pd.nullCheckEnabled){
                        nonExistentProperties.add(pd);
                    }
                }
            }
            return ImmutableMap.copyOf(propDefns);
        }
