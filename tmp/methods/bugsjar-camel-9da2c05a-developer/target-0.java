    public RedeliveryPolicy createRedeliveryPolicy(CamelContext context, RedeliveryPolicy parentPolicy) {

        RedeliveryPolicy answer;
        if (parentPolicy != null) {
            answer = parentPolicy.copy();
        } else {
            answer = new RedeliveryPolicy();
        }

        try {

            // copy across the properties - if they are set
            if (maximumRedeliveries != null) {
                answer.setMaximumRedeliveries(CamelContextHelper.parseInteger(context, maximumRedeliveries));
            }
            if (redeliveryDelay != null) {
                answer.setRedeliveryDelay(CamelContextHelper.parseLong(context, redeliveryDelay));
            }
            if (asyncDelayedRedelivery != null) {
                if (CamelContextHelper.parseBoolean(context, asyncDelayedRedelivery)) {
                    answer.asyncDelayedRedelivery();
                }
            }
            if (retriesExhaustedLogLevel != null) {
                answer.setRetriesExhaustedLogLevel(retriesExhaustedLogLevel);
            }
            if (retryAttemptedLogLevel != null) {
                answer.setRetryAttemptedLogLevel(retryAttemptedLogLevel);
            }
            if (backOffMultiplier != null) {
                answer.setBackOffMultiplier(CamelContextHelper.parseDouble(context, backOffMultiplier));
            }
            if (useExponentialBackOff != null) {
                answer.setUseExponentialBackOff(CamelContextHelper.parseBoolean(context, useExponentialBackOff));
            }
            if (collisionAvoidanceFactor != null) {
                answer.setCollisionAvoidanceFactor(CamelContextHelper.parseDouble(context, collisionAvoidanceFactor));
            }
            if (useCollisionAvoidance != null) {
                answer.setUseCollisionAvoidance(CamelContextHelper.parseBoolean(context, useCollisionAvoidance));
            }
            if (maximumRedeliveryDelay != null) {
                answer.setMaximumRedeliveryDelay(CamelContextHelper.parseLong(context, maximumRedeliveryDelay));
            }
            if (logStackTrace != null) {
                answer.setLogStackTrace(CamelContextHelper.parseBoolean(context, logStackTrace));
            }
            if (logRetryStackTrace != null) {
                answer.setLogRetryStackTrace(CamelContextHelper.parseBoolean(context, logRetryStackTrace));
            }
            if (logHandled != null) {
                answer.setLogHandled(CamelContextHelper.parseBoolean(context, logHandled));
            }
            if (logNewException != null) {
                answer.setLogNewException(CamelContextHelper.parseBoolean(context, logNewException));
            }
            if (logContinued != null) {
                answer.setLogContinued(CamelContextHelper.parseBoolean(context, logContinued));
            }
            if (logRetryAttempted != null) {
                answer.setLogRetryAttempted(CamelContextHelper.parseBoolean(context, logRetryAttempted));
            }
            if (logExhausted != null) {
                answer.setLogExhausted(CamelContextHelper.parseBoolean(context, logExhausted));
            }
            if (logExhaustedMessageHistory != null) {
                answer.setLogExhaustedMessageHistory(CamelContextHelper.parseBoolean(context, logExhaustedMessageHistory));
            }
            if (disableRedelivery != null) {
                if (CamelContextHelper.parseBoolean(context, disableRedelivery)) {
                    answer.setMaximumRedeliveries(0);
                }
            }
            if (delayPattern != null) {
                answer.setDelayPattern(CamelContextHelper.parseText(context, delayPattern));
            }
            if (allowRedeliveryWhileStopping != null) {
                answer.setAllowRedeliveryWhileStopping(CamelContextHelper.parseBoolean(context, allowRedeliveryWhileStopping));
            }
            if (exchangeFormatterRef != null) {
                answer.setExchangeFormatterRef(CamelContextHelper.parseText(context, exchangeFormatterRef));
            }
        } catch (Exception e) {
            throw ObjectHelper.wrapRuntimeCamelException(e);
        }

        return answer;
    }
