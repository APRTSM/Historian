    public static void updateSuggester(Directory directory, Analyzer analyzer, IndexReader reader) throws IOException {
        File tempDir = null;
        try {
            //Analyzing infix suggester takes a file parameter. It uses its path to getDirectory()
            //for actual storage of suggester data. BUT, while building it also does getDirectory() to
            //a temporary location (original path + ".tmp"). So, instead we create a temp dir and also
            //create a placeholder non-existing-sub-child which would mark the location when we want to return
            //our internal suggestion OakDirectory. After build is done, we'd delete the temp directory
            //thereby removing any temp stuff that suggester created in the interim.
            tempDir = Files.createTempDir();
            File tempSubChild = new File(tempDir, "non-existing-sub-child");

            Dictionary dictionary = new LuceneDictionary(reader, FieldNames.SUGGEST);
            getLookup(directory, analyzer, tempSubChild).build(dictionary);
        } catch (RuntimeException e) {
            log.debug("could not update the suggester", e);
        } finally {
            //cleanup temp dir
            if (tempDir != null && !FileUtils.deleteQuietly(tempDir)) {
                log.error("Cleanup failed for temp dir {}", tempDir.getAbsolutePath());
            }
        }
    }
