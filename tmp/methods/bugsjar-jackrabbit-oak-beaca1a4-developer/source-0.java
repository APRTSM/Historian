    private IndexPlan.Builder getPlanBuilder() {
        log.trace("Evaluating plan with index definition {}", defn);
        FullTextExpression ft = filter.getFullTextConstraint();

        if (!defn.getVersion().isAtLeast(IndexFormatVersion.V2)){
            log.trace("Index is old format. Not supported");
            return null;
        }

        //Query Fulltext and Index does not support fulltext
        if (ft != null && !defn.isFullTextEnabled()) {
            return null;
        }

        IndexingRule indexingRule = getApplicableRule();
        if (indexingRule == null){
            return null;
        }

        //Query Fulltext and indexing rule does not support fulltext
        if (ft != null && !indexingRule.isFulltextEnabled()){
            return null;
        }

        result = new PlanResult(indexPath, defn, indexingRule);

        if (defn.hasFunctionDefined()
                && filter.getPropertyRestriction(defn.getFunctionName()) != null) {
            //If native function is handled by this index then ensure
            // that lowest cost if returned
            return defaultPlan().setEstimatedEntryCount(1);
        }

        List<String> indexedProps = newArrayListWithCapacity(filter.getPropertyRestrictions().size());

        //Optimization - Go further only if any of the property is configured
        //for property index
        if (indexingRule.propertyIndexEnabled) {
            for (PropertyRestriction pr : filter.getPropertyRestrictions()) {
                PropertyDefinition pd = indexingRule.getConfig(pr.propertyName);
                if (pd != null && pd.propertyIndexEnabled()) {
                    indexedProps.add(pr.propertyName);
                    result.propDefns.put(pr.propertyName, pd);
                }
            }
        }

        boolean evalPathRestrictions = canEvalPathRestrictions(indexingRule);
        boolean canEvalAlFullText = canEvalAllFullText(indexingRule, ft);

        if (ft != null && !canEvalAlFullText){
            return null;
        }

        //Fulltext expression can also be like jcr:contains(jcr:content/metadata/@format, 'image')

        List<OrderEntry> sortOrder = createSortOrder(indexingRule);
        if (!indexedProps.isEmpty() || !sortOrder.isEmpty() || ft != null || evalPathRestrictions) {
            //TODO Need a way to have better cost estimate to indicate that
            //this index can evaluate more propertyRestrictions natively (if more props are indexed)
            //For now we reduce cost per entry
            int costPerEntryFactor = indexedProps.size();
            costPerEntryFactor += sortOrder.size();

            //this index can evaluate more propertyRestrictions natively (if more props are indexed)
            //For now we reduce cost per entry
            IndexPlan.Builder plan = defaultPlan();
            if (!sortOrder.isEmpty()) {
                plan.setSortOrder(sortOrder);
            }

            if (costPerEntryFactor == 0){
                costPerEntryFactor = 1;
            }

            if (ft == null){
                result.enableNonFullTextConstraints();
            }

            return plan.setCostPerEntry(defn.getCostPerEntry() / costPerEntryFactor);
        }

        //TODO Support for property existence queries
        //TODO support for nodeName queries

        //Above logic would not return any plan for pure nodeType based query like
        //select * from nt:unstructured. We can do that but this is better handled
        //by NodeType index

        return null;
    }
