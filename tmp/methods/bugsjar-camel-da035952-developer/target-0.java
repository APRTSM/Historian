    public Exchange copy(boolean safeCopy) {
        DefaultExchange exchange = new DefaultExchange(this);

        if (hasProperties()) {
            exchange.setProperties(safeCopyProperties(getProperties()));
        }

        if (safeCopy) {
            exchange.getIn().setBody(getIn().getBody());
            exchange.getIn().setFault(getIn().isFault());
            if (getIn().hasHeaders()) {
                exchange.getIn().setHeaders(safeCopyHeaders(getIn().getHeaders()));
                // just copy the attachments here
                exchange.getIn().copyAttachments(getIn());
            }
            if (hasOut()) {
                exchange.getOut().setBody(getOut().getBody());
                exchange.getOut().setFault(getOut().isFault());
                if (getOut().hasHeaders()) {
                    exchange.getOut().setHeaders(safeCopyHeaders(getOut().getHeaders()));
                }
                // Just copy the attachments here
                exchange.getOut().copyAttachments(getOut());
            }
        } else {
            // old way of doing copy which is @deprecated
            // TODO: remove this in Camel 3.0, and always do a safe copy
            exchange.setIn(getIn().copy());
            if (hasOut()) {
                exchange.setOut(getOut().copy());
            }
        }
        exchange.setException(getException());
        return exchange;
    }
