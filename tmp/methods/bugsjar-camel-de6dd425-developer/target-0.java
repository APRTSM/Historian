    protected Processor createOutputsProcessor(RouteContext routeContext, Collection<ProcessorDefinition<?>> outputs) throws Exception {
        List<Processor> list = new ArrayList<Processor>();
        for (ProcessorDefinition<?> output : outputs) {

            // allow any custom logic before we create the processor
            output.preCreateProcessor();

            // resolve properties before we create the processor
            resolvePropertyPlaceholders(routeContext, output);

            // resolve constant fields (eg Exchange.FILE_NAME)
            resolveKnownConstantFields(output);

            // also resolve properties and constant fields on embedded expressions
            ProcessorDefinition<?> me = (ProcessorDefinition<?>) output;
            if (me instanceof ExpressionNode) {
                ExpressionNode exp = (ExpressionNode) me;
                ExpressionDefinition expressionDefinition = exp.getExpression();
                if (expressionDefinition != null) {
                    // resolve properties before we create the processor
                    resolvePropertyPlaceholders(routeContext, expressionDefinition);

                    // resolve constant fields (eg Exchange.FILE_NAME)
                    resolveKnownConstantFields(expressionDefinition);
                }
            }

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
    protected void resolvePropertyPlaceholders(RouteContext routeContext, Object definition) throws Exception {
        log.trace("Resolving property placeholders for: {}", definition);

        // find all getter/setter which we can use for property placeholders
        Map<String, Object> properties = new HashMap<String, Object>();
        IntrospectionSupport.getProperties(definition, properties, null);

        ProcessorDefinition<?> processorDefinition = null;
        if (definition instanceof ProcessorDefinition) {
            processorDefinition = (ProcessorDefinition<?>) definition;
        }
        // include additional properties which have the Camel placeholder QName
        // and when the definition parameter is this (otherAttributes belong to this)
        if (processorDefinition != null && processorDefinition.getOtherAttributes() != null) {
            for (QName key : processorDefinition.getOtherAttributes().keySet()) {
                if (Constants.PLACEHOLDER_QNAME.equals(key.getNamespaceURI())) {
                    String local = key.getLocalPart();
                    Object value = processorDefinition.getOtherAttributes().get(key);
                    if (value != null && value instanceof String) {
                        // value must be enclosed with placeholder tokens
                        String s = (String) value;
                        String prefixToken = routeContext.getCamelContext().getPropertyPrefixToken();
                        String suffixToken = routeContext.getCamelContext().getPropertySuffixToken();
                        if (prefixToken == null) {
                            throw new IllegalArgumentException("Property with name [" + local + "] uses property placeholders; however, no properties component is configured.");
                        }
                        
                        if (!s.startsWith(prefixToken)) {
                            s = prefixToken + s;
                        }
                        if (!s.endsWith(suffixToken)) {
                            s = s + suffixToken;
                        }
                        value = s;
                    }
                    properties.put(local, value);
                }
            }
        }

        if (!properties.isEmpty()) {
            log.trace("There are {} properties on: {}", properties.size(), definition);
            // lookup and resolve properties for String based properties
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                // the name is always a String
                String name = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof String) {
                    // value must be a String, as a String is the key for a property placeholder
                    String text = (String) value;
                    text = routeContext.getCamelContext().resolvePropertyPlaceholders(text);
                    if (text != value) {
                        // invoke setter as the text has changed
                        boolean changed = IntrospectionSupport.setProperty(routeContext.getCamelContext().getTypeConverter(), definition, name, text);
                        if (!changed) {
                            throw new IllegalArgumentException("No setter to set property: " + name + " to: " + text + " on: " + definition);
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("Changed property [{}] from: {} to: {}", new Object[]{name, value, text});
                        }
                    }
                }
            }
        }
    }
