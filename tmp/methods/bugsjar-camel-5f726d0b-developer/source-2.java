    void setMessageHeaderOnBreakpoint(String nodeId, String headerName, String value);
    void setMessageBodyOnBreakpoint(String nodeId, String body);
    public void setMessageHeaderOnBreakpoint(String nodeId, String headerName, String value) {
        backlogDebugger.setMessageHeaderOnBreakpoint(nodeId, headerName, value);
    }
    public void setMessageBodyOnBreakpoint(String nodeId, String body) {
        backlogDebugger.setMessageBodyOnBreakpoint(nodeId, body);
    }
    public void setMessageBodyOnBreakpoint(String nodeId, String body) {
        SuspendedExchange se = suspendedBreakpoints.get(nodeId);
        if (se != null) {
            logger.log("Breakpoint at node " + nodeId + " is updating message body on exchangeId: " + se.getExchange().getExchangeId() + " with new body: " + body);
            if (se.getExchange().hasOut()) {
                se.getExchange().getOut().setBody(body);
            } else {
                se.getExchange().getIn().setBody(body);
            }
        }
    }
    public void setMessageHeaderOnBreakpoint(String nodeId, String headerName, String value) {
        SuspendedExchange se = suspendedBreakpoints.get(nodeId);
        if (se != null) {
            logger.log("Breakpoint at node " + nodeId + " is updating message header on exchangeId: " + se.getExchange().getExchangeId() + " with header: " + headerName + " and value: " + value);
            if (se.getExchange().hasOut()) {
                se.getExchange().getOut().setHeader(headerName, value);
            } else {
                se.getExchange().getIn().setHeader(headerName, value);
            }
        }
    }
