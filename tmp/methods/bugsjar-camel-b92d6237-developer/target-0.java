    private static Map<String, Object> safeCopy(Map<String, Object> properties) {
        if (properties == null) {
            return null;
        }

        // safe copy message history using a defensive copy
        List<MessageHistory> history = (List<MessageHistory>) properties.remove(Exchange.MESSAGE_HISTORY);
        if (history != null) {
            properties.put(Exchange.MESSAGE_HISTORY, new ArrayList<MessageHistory>(history));
        }

        return new ConcurrentHashMap<String, Object>(properties);
    }
