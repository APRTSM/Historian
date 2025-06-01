        public void done(boolean doneSync) {
            try {
                if (!doneSync) {
                    // when done asynchronously then restore information from previous thread
                    if (breadcrumbId != null) {
                        MDC.put(MDC_BREADCRUMB_ID, breadcrumbId);
                    }
                    if (exchangeId != null) {
                        MDC.put(MDC_EXCHANGE_ID, exchangeId);
                    }
                    if (messageId != null) {
                        MDC.put(MDC_MESSAGE_ID, messageId);
                    }
                    if (correlationId != null) {
                        MDC.put(MDC_CORRELATION_ID, correlationId);
                    }
                    if (camelContextId != null) {
                        MDC.put(MDC_CAMEL_CONTEXT_ID, camelContextId);
                    }
                }
                // need to setup the routeId finally
                if (routeId != null) {
                    MDC.put(MDC_ROUTE_ID, routeId);
                }

            } finally {
                // muse ensure delegate is invoked
                delegate.done(doneSync);
            }
        }
