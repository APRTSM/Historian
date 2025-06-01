    public IndexDefinition(NodeState root, NodeState defn, @Nullable NodeBuilder defnb) {
        this.root = root;
        this.version = determineIndexFormatVersion(defn, defnb);
        this.definition = defn;
        this.indexPath = determineIndexPath(defn, defnb);
        this.indexName = indexPath;

        this.blobSize = getOptionalValue(defn, BLOB_SIZE, DEFAULT_BLOB_SIZE);
        this.activeDelete = getOptionalValue(defn, ACTIVE_DELETE, DEFAULT_ACTIVE_DELETE);
        this.testMode = getOptionalValue(defn, LuceneIndexConstants.TEST_MODE, false);

        this.aggregates = collectAggregates(defn);

        NodeState rulesState = defn.getChildNode(LuceneIndexConstants.INDEX_RULES);
        if (!rulesState.exists()){
            rulesState = createIndexRules(defn).getNodeState();
        }

        List<IndexingRule> definedIndexRules = newArrayList();
        this.indexRules = collectIndexRules(rulesState, definedIndexRules);
        this.definedRules = ImmutableList.copyOf(definedIndexRules);

        this.fullTextEnabled = hasFulltextEnabledIndexRule(definedIndexRules);
        this.evaluatePathRestrictions = getOptionalValue(defn, EVALUATE_PATH_RESTRICTION, false);

        String functionName = getOptionalValue(defn, LuceneIndexConstants.FUNC_NAME, null);
        if (fullTextEnabled && functionName == null){
            functionName = "lucene";
        }
        this.funcName = functionName != null ? "native*" + functionName : null;

        this.codec = createCodec();

        if (defn.hasProperty(ENTRY_COUNT_PROPERTY_NAME)) {
            this.entryCountDefined = true;
            this.entryCount = defn.getProperty(ENTRY_COUNT_PROPERTY_NAME).getValue(Type.LONG);
        } else {
            this.entryCountDefined = false;
            this.entryCount = DEFAULT_ENTRY_COUNT;
        }

        this.maxFieldLength = getOptionalValue(defn, LuceneIndexConstants.MAX_FIELD_LENGTH, DEFAULT_MAX_FIELD_LENGTH);
        this.costPerEntry = getOptionalValue(defn, LuceneIndexConstants.COST_PER_ENTRY, 1.0);
        this.costPerExecution = getOptionalValue(defn, LuceneIndexConstants.COST_PER_EXECUTION, 1.0);
        this.indexesAllTypes = areAllTypesIndexed();
        this.analyzers = collectAnalyzers(defn);
        this.analyzer = createAnalyzer();
        this.hasCustomTikaConfig = getTikaConfigNode().exists();
        this.maxExtractLength = determineMaxExtractLength();
        this.suggesterUpdateFrequencyMinutes = evaluateSuggesterUpdateFrequencyMinutes(defn,
                DEFAULT_SUGGESTER_UPDATE_FREQUENCY_MINUTES);
        this.scorerProviderName = getOptionalValue(defn, LuceneIndexConstants.PROP_SCORER_PROVIDER, null);
        this.reindexCount = determineReindexCount(defn, defnb);
        this.pathFilter = PathFilter.from(new ReadOnlyBuilder(defn));
        this.queryPaths = getQueryPaths(defn);
        this.saveDirListing = getOptionalValue(defn, LuceneIndexConstants.SAVE_DIR_LISTING, true);
        this.suggestAnalyzed = evaluateSuggestAnalyzed(defn, false);
        this.secureFacets = defn.hasChildNode(FACETS) && getOptionalValue(defn.getChildNode(FACETS), PROP_SECURE_FACETS, true);
        this.suggestEnabled = evaluateSuggestionEnabled();
        this.spellcheckEnabled = evaluateSpellcheckEnabled();
    }
