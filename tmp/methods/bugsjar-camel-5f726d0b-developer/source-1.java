    void setMessageHeaderOnBreakpoint(String nodeId, String headerName, String value);
    void setMessageBodyOnBreakpoint(String nodeId, String body);
    public void setMessageHeaderOnBreakpoint(String nodeId, String headerName, String value) {
        backlogDebugger.setMessageHeaderOnBreakpoint(nodeId, headerName, value);
    }
    public void setMessageBodyOnBreakpoint(String nodeId, String body) {
        backlogDebugger.setMessageBodyOnBreakpoint(nodeId, body);
    }
