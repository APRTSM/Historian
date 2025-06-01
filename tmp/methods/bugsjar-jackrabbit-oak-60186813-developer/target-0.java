    private Collection<PermissionEntry> loadEntries(@Nonnull String path) {
        Collection<PermissionEntry> ret = new TreeSet<PermissionEntry>();
        for (String name : existingNames) {
            cache.load(store, ret, name, path);
        }
        return ret;
    }
    private void init() {
        long cnt = 0;
        existingNames.clear();
        for (String name : principalNames) {
            long n = cache.getNumEntries(store, name, maxSize);
            /*
            if cache.getNumEntries (n) returns a number bigger than 0, we
            remember this principal name int the 'existingNames' set
            */
            if (n > 0) {
                existingNames.add(name);
            }
            /*
            Calculate the total number of permission entries (cnt) defined for the
            given set of principals in order to be able to determine if the cache
            should be loaded upfront.
            Note however that cache.getNumEntries (n) may return Long.MAX_VALUE
            if the underlying implementation does not know the exact value, and
            the child node count is higher than maxSize (see OAK-2465).
            */
            if (cnt < Long.MAX_VALUE) {
                if (Long.MAX_VALUE == n) {
                    cnt = Long.MAX_VALUE;
                } else {
                    try {
                        cnt = LongMath.checkedAdd(cnt, n);
                    } catch (ArithmeticException ae) {
                        log.warn("Long overflow while calculate the total number of permission entries");
                        cnt = Long.MAX_VALUE;
                    }
                }
            }
        }

        if (cnt > 0 && cnt < maxSize) {
            // the total number of entries is smaller that maxSize, so we can
            // cache all entries for all principals having any entries right away
            pathEntryMap = new HashMap<String, Collection<PermissionEntry>>();
            for (String name : existingNames) {
                cache.load(store, pathEntryMap, name);
            }
        } else {
            pathEntryMap = null;
        }
    }
