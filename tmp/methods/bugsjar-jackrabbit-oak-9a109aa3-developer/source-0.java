    void closeWriter() throws IOException {
        //If reindex or fresh index and write is null on close
        //it indicates that the index is empty. In such a case trigger
        //creation of write such that an empty Lucene index state is persisted
        //in directory
        if (reindex && writer == null){
            getWriter();
        }

        if (writer != null) {
            if (log.isTraceEnabled()) {
                trackIndexSizeInfo(writer, definition, directory);
            }

            final long start = PERF_LOGGER.start();

            updateSuggester(writer.getAnalyzer());
            PERF_LOGGER.end(start, -1, "Completed suggester for directory {}", definition);

            writer.close();
            PERF_LOGGER.end(start, -1, "Closed writer for directory {}", definition);

            directory.close();
            PERF_LOGGER.end(start, -1, "Closed directory for directory {}", definition);

            //OAK-2029 Record the last updated status so
            //as to make IndexTracker detect changes when index
            //is stored in file system
            NodeBuilder status = definitionBuilder.child(":status");
            status.setProperty("lastUpdated", ISO8601.format(getCalendar()), Type.DATE);
            status.setProperty("indexedNodes",indexedNodes);
            PERF_LOGGER.end(start, -1, "Overall Closed IndexWriter for directory {}", definition);

            textExtractionStats.log(reindex);
            textExtractionStats.collectStats(extractedTextCache);
        }
    }
    private void updateSuggester(Analyzer analyzer) throws IOException {

        if (definition.isSuggestEnabled()) {

            boolean updateSuggester = false;
            NodeBuilder suggesterStatus = definitionBuilder.child(":suggesterStatus");
            if (suggesterStatus.hasProperty("lastUpdated")) {
                PropertyState suggesterLastUpdatedValue = suggesterStatus.getProperty("lastUpdated");
                Calendar suggesterLastUpdatedTime = ISO8601.parse(suggesterLastUpdatedValue.getValue(Type.DATE));
                int updateFrequency = definition.getSuggesterUpdateFrequencyMinutes();
                suggesterLastUpdatedTime.add(Calendar.MINUTE, updateFrequency);
                if (getCalendar().after(suggesterLastUpdatedTime)) {
                    updateSuggester = true;
                }
            } else {
                updateSuggester = true;
            }

            if (updateSuggester) {
                DirectoryReader reader = DirectoryReader.open(writer, false);
                final OakDirectory suggestDirectory = new OakDirectory(definitionBuilder, ":suggest-data", definition, false);
                try {
                    SuggestHelper.updateSuggester(suggestDirectory, analyzer, reader);
                    suggesterStatus.setProperty("lastUpdated", ISO8601.format(getCalendar()), Type.DATE);
                } catch (Throwable e) {
                    log.warn("could not update suggester", e);
                } finally {
                    suggestDirectory.close();
                    reader.close();
                }
            }
        }
    }
