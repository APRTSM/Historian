    private static Exchange copyExchangeNoAttachments(Exchange exchange, boolean preserveExchangeId) {
        Exchange answer = ExchangeHelper.createCopy(exchange, preserveExchangeId);
        // we do not want attachments for the splitted sub-messages
        answer.getIn().setAttachments(null);
        return answer;
    }
