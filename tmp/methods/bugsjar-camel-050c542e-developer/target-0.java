    public MethodNotFoundException(Exchange exchange, Object pojo, String methodName) {
        super("Method with name: " + methodName + " not found on bean: " + pojo, exchange);
        this.methodName = methodName;
        this.bean = pojo;
    }
    public MethodNotFoundException(Object pojo, String methodName, Throwable cause) {
        super("Method with name: " + methodName + " not found on bean: " + pojo, null, cause);
        this.methodName = methodName;
        this.bean = pojo;
    }
