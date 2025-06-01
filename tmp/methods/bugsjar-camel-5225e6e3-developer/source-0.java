    public ObjectName getObjectNameForThreadPool(CamelContext context, ThreadPoolExecutor threadPool, String id, String sourceId) throws MalformedObjectNameException {
        StringBuilder buffer = new StringBuilder();
        buffer.append(domainName).append(":");
        buffer.append(KEY_CONTEXT + "=").append(getContextId(context)).append(",");
        buffer.append(KEY_TYPE + "=" + TYPE_THREAD_POOL + ",");
        buffer.append(KEY_NAME + "=").append(id);
        if (sourceId != null) {
            // provide source id if we know it, this helps end user to know where the pool is used
            buffer.append("(").append(sourceId).append(")");
        }
        return createObjectName(buffer);
    }
