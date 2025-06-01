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
    public void setMessageBodyOnBreakpoint(String nodeId, Object body) {
        SuspendedExchange se = suspendedBreakpoints.get(nodeId);
        if (se != null) {
            boolean remove = body == null;
            if (remove) {
                removeMessageBodyOnBreakpoint(nodeId);
            } else {
                Class oldType;
                if (se.getExchange().hasOut()) {
                    oldType = se.getExchange().getOut().getBody() != null ? se.getExchange().getOut().getBody().getClass() : null;
                } else {
                    oldType = se.getExchange().getIn().getBody() != null ? se.getExchange().getIn().getBody().getClass() : null;
                }
                setMessageBodyOnBreakpoint(nodeId, body, oldType);
            }
        }
    }
    public void setMessageBodyOnBreakpoint(String nodeId, Object body, Class type) {
        SuspendedExchange se = suspendedBreakpoints.get(nodeId);
        if (se != null) {
            boolean remove = body == null;
            if (remove) {
                removeMessageBodyOnBreakpoint(nodeId);
            } else {
                logger.log("Breakpoint at node " + nodeId + " is updating message body on exchangeId: " + se.getExchange().getExchangeId() + " with new body: " + body);
                if (se.getExchange().hasOut()) {
                    // preserve type
                    if (type != null) {
                        se.getExchange().getOut().setBody(body, type);
                    } else {
                        se.getExchange().getOut().setBody(body);
                    }
                } else {
                    if (type != null) {
                        se.getExchange().getIn().setBody(body, type);
                    } else {
                        se.getExchange().getIn().setBody(body);
                    }
                }
            }
        }
    }
    public void setMessageHeaderOnBreakpoint(String nodeId, String headerName, Object value, Class type) throws NoTypeConversionAvailableException {
        SuspendedExchange se = suspendedBreakpoints.get(nodeId);
        if (se != null) {
            logger.log("Breakpoint at node " + nodeId + " is updating message header on exchangeId: " + se.getExchange().getExchangeId() + " with header: " + headerName + " and value: " + value);
            if (se.getExchange().hasOut()) {
                if (type != null) {
                    Object convertedValue = se.getExchange().getContext().getTypeConverter().mandatoryConvertTo(type, se.getExchange(), value);
                    se.getExchange().getOut().setHeader(headerName, convertedValue);
                } else {
                    se.getExchange().getOut().setHeader(headerName, value);
                }
            } else {
                if (type != null) {
                    Object convertedValue = se.getExchange().getContext().getTypeConverter().mandatoryConvertTo(type, se.getExchange(), value);
                    se.getExchange().getIn().setHeader(headerName, convertedValue);
                } else {
                    se.getExchange().getIn().setHeader(headerName, value);
                }
            }
        }
    }
    public void setMessageHeaderOnBreakpoint(String nodeId, String headerName, Object value) throws NoTypeConversionAvailableException {
        SuspendedExchange se = suspendedBreakpoints.get(nodeId);
        if (se != null) {
            Class oldType;
            if (se.getExchange().hasOut()) {
                oldType = se.getExchange().getOut().getHeader(headerName) != null ? se.getExchange().getOut().getHeader(headerName).getClass() : null;
            } else {
                oldType = se.getExchange().getIn().getHeader(headerName) != null ? se.getExchange().getIn().getHeader(headerName).getClass() : null;
            }
            setMessageHeaderOnBreakpoint(nodeId, headerName, value, oldType);
        }
    }
    public void removeMessageBodyOnBreakpoint(String nodeId) {
        SuspendedExchange se = suspendedBreakpoints.get(nodeId);
        if (se != null) {
            logger.log("Breakpoint at node " + nodeId + " is removing message body on exchangeId: " + se.getExchange().getExchangeId());
            if (se.getExchange().hasOut()) {
                se.getExchange().getOut().setBody(null);
            } else {
                se.getExchange().getIn().setBody(null);
            }
        }
    }
    public void removeMessageHeaderOnBreakpoint(String nodeId, String headerName) {
        SuspendedExchange se = suspendedBreakpoints.get(nodeId);
        if (se != null) {
            logger.log("Breakpoint at node " + nodeId + " is removing message header on exchangeId: " + se.getExchange().getExchangeId() + " with header: " + headerName);
            if (se.getExchange().hasOut()) {
                se.getExchange().getOut().removeHeader(headerName);
            } else {
                se.getExchange().getIn().removeHeader(headerName);
            }
        }
    }
