    static Map<Revision, String> create(@Nonnull final NodeDocument doc,
                                        @Nonnull final String property) {
        final SortedMap<Revision, String> map = doc.getLocalMap(property);
        if (doc.getPreviousRanges().isEmpty()) {
            return map;
        }
        final Set<Map.Entry<Revision, String>> entrySet
                = new AbstractSet<Map.Entry<Revision, String>>() {

            @Override
            @Nonnull
            public Iterator<Map.Entry<Revision, String>> iterator() {

                final Comparator<? super Revision> c = map.comparator();
                final Iterator<NodeDocument> docs;
                if (map.isEmpty()) {
                    docs = doc.getPreviousDocs(property, null).iterator();
                } else {
                    docs = Iterators.concat(
                            Iterators.singletonIterator(doc),
                            doc.getPreviousDocs(property, null).iterator());
                }

                return new MergeSortedIterators<Map.Entry<Revision, String>>(
                        new Comparator<Map.Entry<Revision, String>>() {
                            @Override
                            public int compare(Map.Entry<Revision, String> o1,
                                               Map.Entry<Revision, String> o2) {
                                return c.compare(o1.getKey(), o2.getKey());
                            }
                        }
                ) {
                    @Override
                    public Iterator<Map.Entry<Revision, String>> nextIterator() {
                        NodeDocument d = docs.hasNext() ? docs.next() : null;
                        if (d == null) {
                            return null;
                        }
                        Map<Revision, String> values;
                        if (Objects.equal(d.getId(), doc.getId())) {
                            // return local map for main document
                            values = d.getLocalMap(property);
                        } else {
                            values = d.getValueMap(property);
                        }
                        return values.entrySet().iterator();
                    }

                    @Override
                    public String description() {
                        return "Revisioned values for property " + doc.getId() + "/" + property + ":";
                    }
                };
            }

            @Override
            public int size() {
                int size = map.size();
                for (NodeDocument prev : doc.getPreviousDocs(property, null)) {
                    size += prev.getValueMap(property).size();
                }
                return size;
            }
        };

        return new AbstractMap<Revision, String>() {

            private final Map<Revision, String> map = doc.getLocalMap(property);

            @Override
            @Nonnull
            public Set<Entry<Revision, String>> entrySet() {
                return entrySet;
            }

            @Override
            public String get(Object key) {
                Revision r = (Revision) key;
                // first check values map of this document
                if (map.containsKey(r)) {
                    return map.get(r);
                }
                for (NodeDocument prev : doc.getPreviousDocs(property, r)) {
                    String value = prev.getValueMap(property).get(r);
                    if (value != null) {
                        return value;
                    }
                }
                // not found or null
                return null;
            }

            @Override
            public boolean containsKey(Object key) {
                // check local map first
                if (map.containsKey(key)) {
                    return true;
                }
                Revision r = (Revision) key;
                for (NodeDocument prev : doc.getPreviousDocs(property, r)) {
                    if (prev.getValueMap(property).containsKey(key)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
