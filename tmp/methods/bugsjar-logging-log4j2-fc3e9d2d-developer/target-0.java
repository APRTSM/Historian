    public static StructuredDataFilter createFilter(@PluginElement("pairs") KeyValuePair[] pairs,
                                                    @PluginAttr("operator") String oper,
                                                    @PluginAttr("onmatch") String match,
                                                    @PluginAttr("onmismatch") String mismatch) {
        if (pairs == null || pairs.length == 0) {
            LOGGER.error("keys and values must be specified for the StructuredDataFilter");
            return null;
        }
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        for (KeyValuePair pair : pairs) {
            String key = pair.getKey();
            if (key == null) {
                LOGGER.error("A null key is not valid in MapFilter");
                continue;
            }
            String value = pair.getValue();
            if (value == null) {
                LOGGER.error("A null value for key " + key + " is not allowed in MapFilter");
                continue;
            }
            List<String> list = map.get(pair.getKey());
            if (list != null) {
                list.add(value);
            } else {
                list = new ArrayList<String>();
                list.add(value);
                map.put(pair.getKey(), list);
            }
        }
        if (map.size() == 0) {
            LOGGER.error("StructuredDataFilter is not configured with any valid key value pairs");
            return null;
        }
        boolean isAnd = oper == null || !oper.equalsIgnoreCase("or");
        Result onMatch = Result.toResult(match);
        Result onMismatch = Result.toResult(mismatch);
        return new StructuredDataFilter(map, isAnd, onMatch, onMismatch);
    }
