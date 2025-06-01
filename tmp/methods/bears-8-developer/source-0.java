    protected Map<?,?> _orderEntries(Map<?,?> input, JsonGenerator gen,
            SerializerProvider provider, Object suppressableValue) throws IOException
    {
        // minor optimization: may already be sorted?
        if (input instanceof SortedMap<?,?>) {
            return input;
        }
        // [databind#1411]: TreeMap does not like null key... (although note that
        //   check above should prevent this code from being called in that case)
        if (input.containsKey(null)) {
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
