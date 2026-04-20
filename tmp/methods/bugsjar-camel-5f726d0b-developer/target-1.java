    void removeMessageBodyOnBreakpoint(String nodeId);
    void setMessageHeaderOnBreakpoint(String nodeId, String headerName, Object value, String type);
    void removeMessageHeaderOnBreakpoint(String nodeId, String headerName);
    void setMessageHeaderOnBreakpoint(String nodeId, String headerName, Object value);
    void setMessageBodyOnBreakpoint(String nodeId, Object body, String type);
    void setMessageBodyOnBreakpoint(String nodeId, Object body);
    public void setMessageBodyOnBreakpoint(String nodeId, Object body) {
        backlogDebugger.setMessageBodyOnBreakpoint(nodeId, body);
    }
    public void setMessageBodyOnBreakpoint(String nodeId, Object body, String type) {
        try {
            Class<?> classType = camelContext.getClassResolver().resolveMandatoryClass(type);
            backlogDebugger.setMessageBodyOnBreakpoint(nodeId, body, classType);
        } catch (ClassNotFoundException e) {
            throw ObjectHelper.wrapRuntimeCamelException(e);
        }
    }
    public void setMessageHeaderOnBreakpoint(String nodeId, String headerName, Object value, String type) {
        try {
            Class<?> classType = camelContext.getClassResolver().resolveMandatoryClass(type);
            backlogDebugger.setMessageHeaderOnBreakpoint(nodeId, headerName, value, classType);
        } catch (Exception e) {
            throw ObjectHelper.wrapRuntimeCamelException(e);
        }
    }
    public void removeMessageHeaderOnBreakpoint(String nodeId, String headerName) {
        backlogDebugger.removeMessageHeaderOnBreakpoint(nodeId, headerName);
    }
    public void setMessageHeaderOnBreakpoint(String nodeId, String headerName, Object value) {
        try {
            backlogDebugger.setMessageHeaderOnBreakpoint(nodeId, headerName, value);
        } catch (NoTypeConversionAvailableException e) {
            throw ObjectHelper.wrapRuntimeCamelException(e);
        }
    }
    public void removeMessageBodyOnBreakpoint(String nodeId) {
        backlogDebugger.removeMessageBodyOnBreakpoint(nodeId);
    }
