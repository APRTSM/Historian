    private void init() {
        long cnt = 0;
        existingNames.clear();
        for (String name: principalNames) {
            long n = cache.getNumEntries(store, name, maxSize);
            cnt+= n;
            if (n > 0) {
                existingNames.add(name);
            }
        }
        if (cnt < maxSize) {
            // cache all entries of all principals
            pathEntryMap = new HashMap<String, Collection<PermissionEntry>>();
            for (String name: principalNames) {
                cache.load(store, pathEntryMap, name);
            }
        } else {
            pathEntryMap = null;
        }
    }
    private Collection<PermissionEntry> loadEntries(@Nonnull String path) {
        Collection<PermissionEntry> ret = new TreeSet<PermissionEntry>();
        for (String name: existingNames) {
            cache.load(store, ret, name, path);
        }
        return ret;
    }
