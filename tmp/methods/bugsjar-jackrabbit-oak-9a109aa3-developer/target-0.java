    private Calendar updateSuggester(Analyzer analyzer) throws IOException {
        Calendar ret = null;
        NodeBuilder suggesterStatus = definitionBuilder.child(":suggesterStatus");
        DirectoryReader reader = DirectoryReader.open(writer, false);
        final OakDirectory suggestDirectory = new OakDirectory(definitionBuilder, ":suggest-data", definition, false);
        try {
            SuggestHelper.updateSuggester(suggestDirectory, analyzer, reader);
            ret = getCalendar();
            suggesterStatus.setProperty("lastUpdated", ISO8601.format(ret), Type.DATE);
        } catch (Throwable e) {
            log.warn("could not update suggester", e);
        } finally {
            suggestDirectory.close();
            reader.close();
        }

        return ret;
    }
    private boolean isIndexUpdatedAfter(Calendar calendar) {
        NodeBuilder indexStats = definitionBuilder.child(":status");
        PropertyState indexLastUpdatedValue = indexStats.getProperty("lastUpdated");
        if (indexLastUpdatedValue != null) {
            Calendar indexLastUpdatedTime = ISO8601.parse(indexLastUpdatedValue.getValue(Type.DATE));
            return indexLastUpdatedTime.after(calendar);
        } else {
            return true;
        }
    }
    void closeWriter() throws IOException {
        //If reindex or fresh index and write is null on close
        //it indicates that the index is empty. In such a case trigger
        //creation of write such that an empty Lucene index state is persisted
        //in directory
        if (reindex && writer == null){
            getWriter();
        }

        boolean updateSuggestions = shouldUpdateSuggestions();
        if (writer == null && updateSuggestions) {
            log.debug("Would update suggester dictionary although no index changes were detected in current cycle");
            getWriter();
        }

        if (writer != null) {
            if (log.isTraceEnabled()) {
                trackIndexSizeInfo(writer, definition, directory);
            }

            final long start = PERF_LOGGER.start();

            Calendar lastUpdated = null;
            if (updateSuggestions) {
                lastUpdated = updateSuggester(writer.getAnalyzer());
                PERF_LOGGER.end(start, -1, "Completed suggester for directory {}", definition);
            }
            if (lastUpdated == null) {
                lastUpdated = getCalendar();
            }

            writer.close();
            PERF_LOGGER.end(start, -1, "Closed writer for directory {}", definition);

            directory.close();
            PERF_LOGGER.end(start, -1, "Closed directory for directory {}", definition);

            //OAK-2029 Record the last updated status so
            //as to make IndexTracker detect changes when index
            //is stored in file system
            NodeBuilder status = definitionBuilder.child(":status");
            status.setProperty("lastUpdated", ISO8601.format(lastUpdated), Type.DATE);
            status.setProperty("indexedNodes", indexedNodes);

            PERF_LOGGER.end(start, -1, "Overall Closed IndexWriter for directory {}", definition);

            textExtractionStats.log(reindex);
            textExtractionStats.collectStats(extractedTextCache);
        }
    }
    private boolean shouldUpdateSuggestions() {
        boolean updateSuggestions = false;

        if (definition.isSuggestEnabled()) {
            NodeBuilder suggesterStatus = definitionBuilder.child(":suggesterStatus");

            PropertyState suggesterLastUpdatedValue = suggesterStatus.getProperty("lastUpdated");

            if (suggesterLastUpdatedValue != null) {
                Calendar suggesterLastUpdatedTime = ISO8601.parse(suggesterLastUpdatedValue.getValue(Type.DATE));

                int updateFrequency = definition.getSuggesterUpdateFrequencyMinutes();
                Calendar nextSuggestUpdateTime = (Calendar)suggesterLastUpdatedTime.clone();
                nextSuggestUpdateTime.add(Calendar.MINUTE, updateFrequency);
                if (getCalendar().after(nextSuggestUpdateTime)) {
                    updateSuggestions = (writer != null || isIndexUpdatedAfter(suggesterLastUpdatedTime));
                }
            } else {
                updateSuggestions = true;
            }
        }

        return updateSuggestions;
    }
