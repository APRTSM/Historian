    private boolean matchCondition(Map<String, MatchPair> condition, URL url, URL param, Invocation invocation) {
        if (url == null) {
            url = new URL(null, null, 0, null);
        }
        Map<String, String> sample = url.toMap();
        boolean result = false;
        for (Map.Entry<String, MatchPair> matchPair : condition.entrySet()) {
            String key = matchPair.getKey();
            String sampleValue;
            //get real invoked method name from invocation
            if (invocation != null && (Constants.METHOD_KEY.equals(key) || Constants.METHODS_KEY.equals(key))) {
                sampleValue = invocation.getMethodName();
            } else {
                sampleValue = sample.get(key);
            }
            if (sampleValue != null) {
                if (!matchPair.getValue().isMatch(sampleValue, param)) {
                    return false;
                } else {
                    result = true;
                }
            } else {
                //not pass the condition
                if (matchPair.getValue().matches.size() > 0) {
                    return false;
                } else {
                    result = true;
                }
            }
        }
        return result;
    }
