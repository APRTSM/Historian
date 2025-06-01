    public RedeliveryPolicy createRedeliveryPolicy(CamelContext context, RedeliveryPolicy parentPolicy) {
        if (redeliveryPolicyRef != null) {
            return CamelContextHelper.mandatoryLookup(context, redeliveryPolicyRef, RedeliveryPolicy.class);
        } else if (redeliveryPolicy != null) {
            return redeliveryPolicy.createRedeliveryPolicy(context, parentPolicy);
        } else if (!outputs.isEmpty() && parentPolicy.getMaximumRedeliveries() != 0) {
            // if we have outputs, then do not inherit parent maximumRedeliveries
            // as you would have to explicit configure maximumRedeliveries on this onException to use it
            // this is the behavior Camel has always had
            RedeliveryPolicy answer = parentPolicy.copy();
            answer.setMaximumRedeliveries(0);
            return answer;
        } else {
            return parentPolicy;
        }
    }
