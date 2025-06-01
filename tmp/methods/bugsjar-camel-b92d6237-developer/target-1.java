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
    private static Exchange copyExchangeNoAttachments(Exchange exchange, boolean preserveExchangeId) {
        Exchange answer = ExchangeHelper.createCopy(exchange, preserveExchangeId);
        // we do not want attachments for the splitted sub-messages
        answer.getIn().setAttachments(null);
        // we do not want to copy the message history for splitted sub-messages
        answer.getProperties().remove(Exchange.MESSAGE_HISTORY);
        return answer;
    }
