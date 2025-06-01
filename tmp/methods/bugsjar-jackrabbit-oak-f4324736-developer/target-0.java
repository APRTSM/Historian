    private void initializeExtractedTextCache(BundleContext bundleContext, Map<String, ?> config) {
        int cacheSizeInMB = PropertiesUtil.toInteger(config.get(PROP_EXTRACTED_TEXT_CACHE_SIZE),
                PROP_EXTRACTED_TEXT_CACHE_SIZE_DEFAULT);
        int cacheExpiryInSecs = PropertiesUtil.toInteger(config.get(PROP_EXTRACTED_TEXT_CACHE_EXPIRY),
                PROP_EXTRACTED_TEXT_CACHE_EXPIRY_DEFAULT);

        extractedTextCache = new ExtractedTextCache(cacheSizeInMB * ONE_MB, cacheExpiryInSecs);
        if (extractedTextProvider != null){
            registerExtractedTextProvider(extractedTextProvider);
        }
        CacheStats stats = extractedTextCache.getCacheStats();
        if (stats != null){
            oakRegs.add(registerMBean(whiteboard,
                    CacheStatsMBean.class, stats,
                    CacheStatsMBean.TYPE, stats.getName()));
            log.info("Extracted text caching enabled with maxSize {} MB, expiry time {} secs",
                    cacheSizeInMB, cacheExpiryInSecs);
        }
    }
