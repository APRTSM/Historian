    public Object getInjectionPropertyValue(Class<?> type, String propertyName, String propertyDefaultValue,
                                            String injectionPointName, Object bean, String beanName) {
        try {
            // enforce a properties component to be created if none existed
            CamelContextHelper.lookupPropertiesComponent(getCamelContext(), true);

            String key;
            String prefix = getCamelContext().getPropertyPrefixToken();
            String suffix = getCamelContext().getPropertySuffixToken();
            if (!propertyName.contains(prefix)) {
                // must enclose the property name with prefix/suffix to have it resolved
                key = prefix + propertyName + suffix;
            } else {
                // key has already prefix/suffix so use it as-is as it may be a compound key
                key = propertyName;
            }
            String value = getCamelContext().resolvePropertyPlaceholders(key);
            if (value != null) {
                return getCamelContext().getTypeConverter().mandatoryConvertTo(type, value);
            } else {
                return null;
            }
        } catch (Exception e) {
            if (ObjectHelper.isNotEmpty(propertyDefaultValue)) {
                try {
                    return getCamelContext().getTypeConverter().mandatoryConvertTo(type, propertyDefaultValue);
                } catch (Exception e2) {
                    throw ObjectHelper.wrapRuntimeCamelException(e2);
                }
            }
            throw ObjectHelper.wrapRuntimeCamelException(e);
        }
    }
    protected Component lookupPropertiesComponent() {
        return CamelContextHelper.lookupPropertiesComponent(this, false);
    }
    private void doStartCamel() throws Exception {
        if (applicationContextClassLoader == null) {
            // Using the TCCL as the default value of ApplicationClassLoader
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                // use the classloader that loaded this class
                cl = this.getClass().getClassLoader();
            }
            setApplicationContextClassLoader(cl);
        }

        if (log.isDebugEnabled()) {
            log.debug("Using ClassResolver={}, PackageScanClassResolver={}, ApplicationContextClassLoader={}",
                    new Object[]{getClassResolver(), getPackageScanClassResolver(), getApplicationContextClassLoader()});
        }

        if (isStreamCaching()) {
            log.info("StreamCaching is enabled on CamelContext: {}", getName());
        }

        if (isTracing()) {
            // tracing is added in the DefaultChannel so we can enable it on the fly
            log.info("Tracing is enabled on CamelContext: {}", getName());
        }

        if (isUseMDCLogging()) {
            // log if MDC has been enabled
            log.info("MDC logging is enabled on CamelContext: {}", getName());
        }

        if (isHandleFault()) {
            // only add a new handle fault if not already configured
            if (HandleFault.getHandleFault(this) == null) {
                log.info("HandleFault is enabled on CamelContext: {}", getName());
                addInterceptStrategy(new HandleFault());
            }
        }

        if (getDelayer() != null && getDelayer() > 0) {
            log.info("Delayer is enabled with: {} ms. on CamelContext: {}", getDelayer(), getName());
        }
        
        // register debugger
        if (getDebugger() != null) {
            log.info("Debugger: {} is enabled on CamelContext: {}", getDebugger(), getName());
            // register this camel context on the debugger
            getDebugger().setCamelContext(this);
            startService(getDebugger());
            addInterceptStrategy(new Debug(getDebugger()));
        }

        // start management strategy before lifecycles are started
        ManagementStrategy managementStrategy = getManagementStrategy();
        // inject CamelContext if aware
        if (managementStrategy instanceof CamelContextAware) {
            ((CamelContextAware) managementStrategy).setCamelContext(this);
        }
        ServiceHelper.startService(managementStrategy);

        // start lifecycle strategies
        ServiceHelper.startServices(lifecycleStrategies);
        Iterator<LifecycleStrategy> it = lifecycleStrategies.iterator();
        while (it.hasNext()) {
            LifecycleStrategy strategy = it.next();
            try {
                strategy.onContextStart(this);
            } catch (VetoCamelContextStartException e) {
                // okay we should not start Camel since it was vetoed
                log.warn("Lifecycle strategy vetoed starting CamelContext ({}) due {}", getName(), e.getMessage());
                throw e;
            } catch (Exception e) {
                log.warn("Lifecycle strategy " + strategy + " failed starting CamelContext ({}) due {}", getName(), e.getMessage());
                throw e;
            }
        }

        // start notifiers as services
        for (EventNotifier notifier : getManagementStrategy().getEventNotifiers()) {
            if (notifier instanceof Service) {
                Service service = (Service) notifier;
                for (LifecycleStrategy strategy : lifecycleStrategies) {
                    strategy.onServiceAdd(this, service, null);
                }
            }
            if (notifier instanceof Service) {
                startService((Service)notifier);
            }
        }

        // must let some bootstrap service be started before we can notify the starting event
        EventHelper.notifyCamelContextStarting(this);

        forceLazyInitialization();

        // re-create endpoint registry as the cache size limit may be set after the constructor of this instance was called.
        // and we needed to create endpoints up-front as it may be accessed before this context is started
        endpoints = new EndpointRegistry(this, endpoints);
        addService(endpoints);
        // special for executorServiceManager as want to stop it manually
        doAddService(executorServiceManager, false);
        addService(producerServicePool);
        addService(inflightRepository);
        addService(shutdownStrategy);
        addService(packageScanClassResolver);
        addService(restRegistry);

        if (runtimeEndpointRegistry != null) {
            if (runtimeEndpointRegistry instanceof EventNotifier) {
                getManagementStrategy().addEventNotifier((EventNotifier) runtimeEndpointRegistry);
            }
            addService(runtimeEndpointRegistry);
        }

        // eager lookup any configured properties component to avoid subsequent lookup attempts which may impact performance
        // due we use properties component for property placeholder resolution at runtime
        Component existing = CamelContextHelper.lookupPropertiesComponent(this, false);
        if (existing != null) {
            // store reference to the existing properties component
            if (existing instanceof PropertiesComponent) {
                propertiesComponent = (PropertiesComponent) existing;
            } else {
                // properties component must be expected type
                throw new IllegalArgumentException("Found properties component of type: " + existing.getClass() + " instead of expected: " + PropertiesComponent.class);
            }
        }

        // start components
        startServices(components.values());

        // start the route definitions before the routes is started
        startRouteDefinitions(routeDefinitions);

        // is there any stream caching enabled then log an info about this and its limit of spooling to disk, so people is aware of this
        boolean streamCachingInUse = isStreamCaching();
        if (!streamCachingInUse) {
            for (RouteDefinition route : routeDefinitions) {
                Boolean routeCache = CamelContextHelper.parseBoolean(this, route.getStreamCache());
                if (routeCache != null && routeCache) {
                    streamCachingInUse = true;
                    break;
                }
            }
        }

        if (isAllowUseOriginalMessage()) {
            log.info("AllowUseOriginalMessage is enabled. If access to the original message is not needed,"
                    + " then its recommended to turn this option off as it may improve performance.");
        }

        if (streamCachingInUse) {
            // stream caching is in use so enable the strategy
            getStreamCachingStrategy().setEnabled(true);
            addService(getStreamCachingStrategy());
        } else {
            // log if stream caching is not in use as this can help people to enable it if they use streams
            log.info("StreamCaching is not in use. If using streams then its recommended to enable stream caching."
                    + " See more details at http://camel.apache.org/stream-caching.html");
        }

        // start routes
        if (doNotStartRoutesOnFirstStart) {
            log.debug("Skip starting of routes as CamelContext has been configured with autoStartup=false");
        }

        // invoke this logic to warmup the routes and if possible also start the routes
        doStartOrResumeRoutes(routeServices, true, !doNotStartRoutesOnFirstStart, false, true);

        // starting will continue in the start method
    }
    public String resolvePropertyPlaceholders(String text) throws Exception {
        // While it is more efficient to only do the lookup if we are sure we need the component,
        // with custom tokens, we cannot know if the URI contains a property or not without having
        // the component.  We also lose fail-fast behavior for the missing component with this change.
        PropertiesComponent pc = getPropertiesComponent();

        // Do not parse uris that are designated for the properties component as it will handle that itself
        if (text != null && !text.startsWith("properties:")) {
            // No component, assume default tokens.
            if (pc == null && text.contains(PropertiesComponent.DEFAULT_PREFIX_TOKEN)) {
                // lookup existing properties component, or force create a new default component
                pc = (PropertiesComponent) CamelContextHelper.lookupPropertiesComponent(this, true);
            }

            if (pc != null && text.contains(pc.getPrefixToken())) {
                // the parser will throw exception if property key was not found
                String answer = pc.parseUri(text);
                log.debug("Resolved text: {} -> {}", text, answer);
                return answer; 
            }
        }

        // return original text as is
        return text;
    }
    public static void resolvePropertyPlaceholders(RouteContext routeContext, Object definition) throws Exception {
        LOG.trace("Resolving property placeholders for: {}", definition);

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
                        // enforce a properties component to be created if none existed
                        CamelContextHelper.lookupPropertiesComponent(routeContext.getCamelContext(), true);

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
            LOG.trace("There are {} properties on: {}", properties.size(), definition);
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
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Changed property [{}] from: {} to: {}", new Object[]{name, value, text});
                        }
                    }
                }
            }
        }
    }
    public static Component lookupPropertiesComponent(CamelContext camelContext, boolean autoCreate) {
        // no existing properties component so lookup and add as component if possible
        PropertiesComponent answer = (PropertiesComponent) camelContext.hasComponent("properties");
        if (answer == null) {
            answer = camelContext.getRegistry().lookupByNameAndType("properties", PropertiesComponent.class);
            if (answer != null) {
                camelContext.addComponent("properties", answer);
            }
        }
        if (answer == null && autoCreate) {
            // create a default properties component to be used as there may be default values we can use
            LOG.info("No existing PropertiesComponent has been configured, creating a new default PropertiesComponent with name: properties");
            answer = camelContext.getComponent("properties", PropertiesComponent.class);
        }
        return answer;
    }
