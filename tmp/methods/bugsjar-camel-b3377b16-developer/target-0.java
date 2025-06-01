    protected static void doProcess(Processor processor, Exchange exchange) {
        // must remember some properties which we cannot use during onCompletion processing
        // as otherwise we may cause issues
        Object stop = exchange.removeProperty(Exchange.ROUTE_STOP);
        Object failureHandled = exchange.removeProperty(Exchange.FAILURE_HANDLED);
        Object caught = exchange.removeProperty(Exchange.EXCEPTION_CAUGHT);
        Object errorhandlerHandled = exchange.removeProperty(Exchange.ERRORHANDLER_HANDLED);
        Object rollbackOnly = exchange.removeProperty(Exchange.ROLLBACK_ONLY);
        Object rollbackOnlyLast = exchange.removeProperty(Exchange.ROLLBACK_ONLY_LAST);

        Exception cause = exchange.getException();
        exchange.setException(null);

        try {
            processor.process(exchange);
        } catch (Exception e) {
            exchange.setException(e);
        } finally {
            // restore the options
            if (stop != null) {
                exchange.setProperty(Exchange.ROUTE_STOP, stop);
            }
            if (failureHandled != null) {
                exchange.setProperty(Exchange.FAILURE_HANDLED, failureHandled);
            }
            if (caught != null) {
                exchange.setProperty(Exchange.EXCEPTION_CAUGHT, caught);
            }
            if (errorhandlerHandled != null) {
                exchange.setProperty(Exchange.ERRORHANDLER_HANDLED, errorhandlerHandled);
            }
            if (rollbackOnly != null) {
                exchange.setProperty(Exchange.ROLLBACK_ONLY, rollbackOnly);
            }
            if (rollbackOnlyLast != null) {
                exchange.setProperty(Exchange.ROLLBACK_ONLY, rollbackOnlyLast);
            }
            if (cause != null) {
                exchange.setException(cause);
            }
        }
    }
