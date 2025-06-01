    private Document makeDocument(String path, NodeState state, boolean isUpdate) {
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
        dirty |= indexNullCheckEnabledProps(path, fields, state);
        dirty |= indexNotNullCheckEnabledProps(path, fields, state);

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

        // because of LUCENE-5833 we have to merge the suggest fields into a single one
        Field suggestField = null;
        for (Field f : fields) {
            if (FieldNames.SUGGEST.endsWith(f.name())) {
                if (suggestField == null) {
                    suggestField = FieldFactory.newSuggestField(f.stringValue());
                } else {
                    suggestField = FieldFactory.newSuggestField(suggestField.stringValue(), f.stringValue());
                }
            } else {
                document.add(f);
            }
        }
        if (suggestField != null) {
            document.add(suggestField);
        }

        //TODO Boost at document level

        return document;
    }
    public void propertyChanged(PropertyState before, PropertyState after) {
        markPropertyChanged(before.getName());
        checkAggregates(before.getName());
    }
