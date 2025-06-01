    protected Map<?,?> _orderEntries(Map<?,?> input, JsonGenerator gen,
            SerializerProvider provider, Object suppressableValue) throws IOException
    {
        // minor optimization: may already be sorted?
        if (input instanceof SortedMap<?,?>) {
            return input;
        }
        // [databind#1411]: TreeMap does not like null key... (although note that
        //   check above should prevent this code from being called in that case)
        // [databind#153]: but, apparently, some custom Maps do manage hit this
        //   problem.
        if (_hasNullKey(input)) {
            TreeMap<Object,Object> result = new TreeMap<Object,Object>();
            for (Map.Entry<?,?> entry : input.entrySet()) {
                Object key = entry.getKey();
                if (key == null) {
                    _writeNullKeyedEntry(gen, provider, suppressableValue, entry.getValue());
                    continue;
                } 
                result.put(key, entry.getValue());
            }
            return result;
        }
        return new TreeMap<Object,Object>(input);
    }
    protected boolean _hasNullKey(Map<?,?> input) {
        // 19-Feb-2017, tatu: As per [databind#1513] there are many cases where `null`
        //   keys are not allowed, and even attempt to check for presence can cause
        //   problems. Without resorting to external sorting (and internal API change),
        //   or custom sortable Map implementation (more code) we can try black- or
        //   white-listing (that is; either skip known problem cases; or only apply for
        //   known good cases).
        //   While my first instinct was to do black-listing (remove Hashtable and ConcurrentHashMap),
        //   all in all it is probably better to just white list `HashMap` (and its sub-classes).

        return (input instanceof HashMap) && input.containsKey(null);
    }
