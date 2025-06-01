    public FlumeEvent(final LogEvent event, final String includes, final String excludes, final String required,
                      String mdcPrefix, String eventPrefix, final boolean compress) {
        this.event = event;
        this.compress = compress;
        final Map<String, String> headers = getHeaders();
        headers.put(TIMESTAMP, Long.toString(event.getMillis()));
        if (mdcPrefix == null) {
            mdcPrefix = DEFAULT_MDC_PREFIX;
        }
        if (eventPrefix == null) {
            eventPrefix = DEFAULT_EVENT_PREFIX;
        }
        final Map<String, String> mdc = event.getContextMap();
        if (includes != null) {
            final String[] array = includes.split(",");
            if (array.length > 0) {
                for (String str : array) {
                    str = str.trim();
                    if (mdc.containsKey(str)) {
                        ctx.put(str, mdc.get(str));
                    }
                }
            }
        } else if (excludes != null) {
            final String[] array = excludes.split(",");
            if (array.length > 0) {
                final List<String> list = new ArrayList<String>(array.length);
                for (final String value : array) {
                    list.add(value.trim());
                }
                for (final Map.Entry<String, String> entry : mdc.entrySet()) {
                    if (!list.contains(entry.getKey())) {
                        ctx.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        } else {
            ctx.putAll(mdc);
        }

        if (required != null) {
            final String[] array = required.split(",");
            if (array.length > 0) {
                for (String str : array) {
                    str = str.trim();
                    if (!mdc.containsKey(str)) {
                        throw new LoggingException("Required key " + str + " is missing from the MDC");
                    }
                }
            }
        }
        final String guid =  UUIDUtil.getTimeBasedUUID().toString();
        final Message message = event.getMessage();
        if (message instanceof MapMessage) {
            // Add the guid to the Map so that it can be included in the Layout.
            ((MapMessage) message).put(GUID, guid);
            if (message instanceof StructuredDataMessage) {
                addStructuredData(eventPrefix, headers, (StructuredDataMessage) message);
            }
            addMapData(eventPrefix, headers, (MapMessage) message);
        } else {
            headers.put(GUID, guid);
        }

        addContextData(mdcPrefix, headers, ctx);
    }
