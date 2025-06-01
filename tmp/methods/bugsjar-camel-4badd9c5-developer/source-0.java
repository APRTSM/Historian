    protected Processor createOutputsProcessor(RouteContext routeContext, Collection<ProcessorDefinition> outputs) throws Exception {
        List<Processor> list = new ArrayList<Processor>();
        for (ProcessorDefinition<?> output : outputs) {
            Processor processor = null;
            // at first use custom factory
            if (routeContext.getCamelContext().getProcessorFactory() != null) {
                processor = routeContext.getCamelContext().getProcessorFactory().createProcessor(routeContext, output);
            }
            // fallback to default implementation if factory did not create the processor
            if (processor == null) {
                processor = output.createProcessor(routeContext);
            }

            if (output instanceof Channel && processor == null) {
                continue;
            }

            Processor channel = wrapChannel(routeContext, processor, output);
            list.add(channel);
        }

        // if more than one output wrap than in a composite processor else just keep it as is
        Processor processor = null;
        if (!list.isEmpty()) {
            if (list.size() == 1) {
                processor = list.get(0);
            } else {
                processor = createCompositeProcessor(routeContext, list);
            }
        }

        return processor;
    }
    protected Processor makeProcessor(RouteContext routeContext) throws Exception {
        Processor processor = null;

        // resolve properties before we create the processor
        resolvePropertyPlaceholders(routeContext);

        // at first use custom factory
        if (routeContext.getCamelContext().getProcessorFactory() != null) {
            processor = routeContext.getCamelContext().getProcessorFactory().createProcessor(routeContext, this);
        }
        // fallback to default implementation if factory did not create the processor
        if (processor == null) {
            processor = createProcessor(routeContext);
        }

        if (processor == null) {
            // no processor to make
            return null;
        }
        return wrapProcessor(routeContext, processor);
    }
    protected void resolvePropertyPlaceholders(RouteContext routeContext) throws Exception {
        if (log.isTraceEnabled()) {
            log.trace("Resolving property placeholders for: " + this);
        }

        // find all String getter/setter
        Map<Object, Object> properties = new HashMap<Object, Object>();
        IntrospectionSupport.getProperties(this, properties, null);

        if (!properties.isEmpty()) {
            if (log.isTraceEnabled()) {
                log.trace("There are " + properties.size() + " properties on: " + this);
            }

            // lookup and resolve properties for String based properties
            for (Map.Entry entry : properties.entrySet()) {
                // the name is always a String
                String name = (String) entry.getKey();
                Object value = entry.getValue();
                if (value instanceof String) {
                    // we can only resolve String typed values
                    String text = (String) value;
                    text = routeContext.getCamelContext().resolvePropertyPlaceholders(text);
                    if (text != value) {
                        // invoke setter as the text has changed
                        IntrospectionSupport.setProperty(this, name, text);
                        if (log.isDebugEnabled()) {
                            log.debug("Changed property [" + name + "] from: " + value + " to: " + text);
                        }
                    }
                }
            }
        }
    }
