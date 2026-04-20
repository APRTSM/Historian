    private Result filter() {
        boolean match = false;
        if (useMap) {
            for (final Map.Entry<String, List<String>> entry : getMap().entrySet()) {
                final String toMatch = ThreadContext.get(entry.getKey());
                if (toMatch != null) {
                    match = entry.getValue().contains(toMatch);
                } else {
                    match = false;
                }
                if ((!isAnd() && match) || (isAnd() && !match)) {
                    break;
                }
            }
        } else {
            match = key.equals(ThreadContext.get(key));
        }
        return match ? onMatch : onMismatch;
    }
