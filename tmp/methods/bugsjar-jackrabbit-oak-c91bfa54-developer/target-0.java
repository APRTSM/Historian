    public Iterator<String> getAllChunkIds(final long maxLastModifiedTime) throws Exception {
        return transform(filter(delegate.getAllIdentifiers(), new Predicate<DataIdentifier>() {
            @Override
            public boolean apply(DataIdentifier input) {
                try {
                    DataRecord dr = delegate.getRecord(input);
                    if(dr != null && (maxLastModifiedTime <=0
                            || dr.getLastModified() < maxLastModifiedTime)){
                        return true;
                    }
                } catch (DataStoreException e) {
                    log.warn("Error occurred while fetching DataRecord for identifier {}",input, e);
                }
                return false;
            }
        }),new Function<DataIdentifier, String>() {
            @Override
            public String apply(DataIdentifier input) {
                return input.toString();
            }
        });
    }
