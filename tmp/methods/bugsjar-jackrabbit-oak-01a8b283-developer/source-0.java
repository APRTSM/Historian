    public Iterator<String> getAllChunkIds(long maxLastModifiedTime) throws Exception {
        //TODO Ignores the maxLastModifiedTime currently.
        return Iterators.transform(delegate.getAllIdentifiers(), new Function<DataIdentifier, String>() {
            @Nullable
            @Override
            public String apply(@Nullable DataIdentifier input) {
                return input.toString();
            }
        });
    }
