    private boolean indexAggregates(final String path, final List<Field> fields,
                                    final NodeState state) throws CommitFailedException {
        final AtomicBoolean dirtyFlag = new AtomicBoolean();
        indexingRule.getAggregate().collectAggregates(state, new Aggregate.ResultCollector() {
            @Override
            public void onResult(Aggregate.NodeIncludeResult result) throws CommitFailedException {
                boolean dirty = indexAggregatedNode(path, fields, result);
                if (dirty) {
                    dirtyFlag.set(true);
                }
            }

            @Override
            public void onResult(Aggregate.PropertyIncludeResult result) throws CommitFailedException {
                boolean dirty = false;
                if (result.pd.ordered) {
                    dirty |= addTypedOrderedFields(fields, result.propertyState,
                            result.propertyPath, result.pd);
                }
                dirty |= indexProperty(path, fields, state, result.propertyState,
                        result.propertyPath, result.pd);

                if (dirty) {
                    dirtyFlag.set(true);
                }
            }
        });
        return dirtyFlag.get();
    }
    private Document makeDocument(String path, NodeState state, boolean isUpdate) throws CommitFailedException {
        if (!isIndexable()) {
            return null;
        }

        List<Field> fields = new ArrayList<Field>();
        boolean dirty = false;
        for (PropertyState property : state.getProperties()) {
            String pname = property.getName();

            if (!isVisible(pname)) {
                continue;
            }

            PropertyDefinition pd = indexingRule.getConfig(pname);

            if (pd == null || !pd.index){
                continue;
            }

            if (pd.ordered) {
                dirty |= addTypedOrderedFields(fields, property, pname, pd);
            }

            dirty |= indexProperty(path, fields, state, property, pname, pd);
        }

        dirty |= indexAggregates(path, fields, state);

        if (isUpdate && !dirty) {
            // updated the state but had no relevant changes
            return null;
        }

        //For property index no use making an empty document if
        //none of the properties are indexed
        if(!indexingRule.isFulltextEnabled() && !dirty){
            return null;
        }

        Document document = new Document();
        document.add(newPathField(path));
        String name = getName(path);

        //TODO Possibly index nodeName without tokenization for node name based queries
        if (indexingRule.isFulltextEnabled()) {
            document.add(newFulltextField(name));
        }

        if (getDefinition().evaluatePathRestrictions()){
            document.add(newAncestorsField(PathUtils.getParentPath(path)));
            document.add(newDepthField(path));
        }

        for (Field f : fields) {
            document.add(f);
        }

        //TODO Boost at document level

        return document;
    }
    private boolean indexProperty(String path,
                                  List<Field> fields,
                                  NodeState state,
                                  PropertyState property,
                                  String pname,
                                  PropertyDefinition pd) throws CommitFailedException {
        boolean includeTypeForFullText = indexingRule.includePropertyType(property.getType().tag());
        if (Type.BINARY.tag() == property.getType().tag()
                && includeTypeForFullText) {
            this.context.indexUpdate();
            fields.addAll(newBinary(property, state, null, path + "@" + pname));
            return true;
        }  else {
            boolean dirty = false;

            if (pd.propertyIndex && pd.includePropertyType(property.getType().tag())){
                dirty |= addTypedFields(fields, property, pname);
            }

            if (pd.fulltextEnabled() && includeTypeForFullText) {
                for (String value : property.getValue(Type.STRINGS)) {
                    this.context.indexUpdate();
                    if (pd.analyzed && pd.includePropertyType(property.getType().tag())) {
                        String analyzedPropName = constructAnalyzedPropertyName(pname);
                        fields.add(newPropertyField(analyzedPropName, value, !pd.skipTokenization(pname), pd.stored));
                    }

                    if (pd.nodeScopeIndex) {
                        Field field = newFulltextField(value);
                        field.setBoost(pd.boost);
                        fields.add(field);
                    }
                    dirty = true;
                }
            }
            return dirty;
        }
    }
