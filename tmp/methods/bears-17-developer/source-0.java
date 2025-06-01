    public Collection<NamedType> collectAndResolveSubtypesByTypeId(MapperConfig<?> config, 
            AnnotatedMember property, JavaType baseType)
    {
        final AnnotationIntrospector ai = config.getAnnotationIntrospector();
        Class<?> rawBase = (baseType == null) ? property.getRawType() : baseType.getRawClass();

        // Need to keep track of classes that have been handled already 
        Set<Class<?>> typesHandled = new HashSet<Class<?>>();
        Map<String,NamedType> byName = new LinkedHashMap<String,NamedType>();

        // start with lowest-precedence, which is from type hierarchy
        NamedType rootType = new NamedType(rawBase, null);
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config,
                rawBase);
        _collectAndResolveByTypeId(ac, rootType, config, typesHandled, byName);
        
        // then with definitions from property
        Collection<NamedType> st = ai.findSubtypes(property);
        if (st != null) {
            for (NamedType nt : st) {
                ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, nt.getType());
                _collectAndResolveByTypeId(ac, nt, config, typesHandled, byName);
            }            
        }
        
        // and finally explicit type registrations (highest precedence)
        if (_registeredSubtypes != null) {
            for (NamedType subtype : _registeredSubtypes) {
                // is it a subtype of root type?
                if (rawBase.isAssignableFrom(subtype.getType())) { // yes
                    AnnotatedClass curr = AnnotatedClassResolver.resolveWithoutSuperTypes(config,
                            subtype.getType());
                    _collectAndResolveByTypeId(curr, subtype, config, typesHandled, byName);
                }
            }
        }
        return _combineNamedAndUnnamed(typesHandled, byName);
    }
    public Collection<NamedType> collectAndResolveSubtypesByTypeId(MapperConfig<?> config,
            AnnotatedClass type)
    {
        Set<Class<?>> typesHandled = new HashSet<Class<?>>();
        Map<String,NamedType> byName = new LinkedHashMap<String,NamedType>();

        NamedType rootType = new NamedType(type.getRawType(), null);
        _collectAndResolveByTypeId(type, rootType, config, typesHandled, byName);
        
        if (_registeredSubtypes != null) {
            Class<?> rawBase = type.getRawType();
            for (NamedType subtype : _registeredSubtypes) {
                // is it a subtype of root type?
                if (rawBase.isAssignableFrom(subtype.getType())) { // yes
                    AnnotatedClass curr = AnnotatedClassResolver.resolveWithoutSuperTypes(config,
                            subtype.getType());
                    _collectAndResolveByTypeId(curr, subtype, config, typesHandled, byName);
                }
            }
        }
        return _combineNamedAndUnnamed(typesHandled, byName);
    }
    protected Collection<NamedType> _combineNamedAndUnnamed(Set<Class<?>> typesHandled,
            Map<String,NamedType> byName)
    {
        ArrayList<NamedType> result = new ArrayList<NamedType>(byName.values());

        // Ok, so... we will figure out which classes have no explicitly assigned name,
        // by removing Classes from Set. And for remaining classes, add an anonymous
        // marker
        for (NamedType t : byName.values()) {
            typesHandled.remove(t.getType());
        }
        for (Class<?> cls : typesHandled) {
            result.add(new NamedType(cls));
        }
        return result;
    }
    public Collection<NamedType> collectAndResolveSubtypesByClass(MapperConfig<?> config, 
            AnnotatedMember property, JavaType baseType)
    {
        final AnnotationIntrospector ai = config.getAnnotationIntrospector();
        // for backwards compatibility, must allow null here:
        Class<?> rawBase = (baseType == null) ? property.getRawType() : baseType.getRawClass();
        
        HashMap<NamedType, NamedType> collected = new HashMap<NamedType, NamedType>();
        // start with registered subtypes (which have precedence)
        if (_registeredSubtypes != null) {
            for (NamedType subtype : _registeredSubtypes) {
                // is it a subtype of root type?
                if (rawBase.isAssignableFrom(subtype.getType())) { // yes
                    AnnotatedClass curr = AnnotatedClassResolver.resolveWithoutSuperTypes(config,
                            subtype.getType());
                    _collectAndResolve(curr, subtype, config, ai, collected);
                }
            }
        }
        
        // then annotated types for property itself
        Collection<NamedType> st = ai.findSubtypes(property);
        if (st != null) {
            for (NamedType nt : st) {
                AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config,
                        nt.getType());
                _collectAndResolve(ac, nt, config, ai, collected);
            }            
        }
        
        NamedType rootType = new NamedType(rawBase, null);
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, rawBase);
            
        // and finally subtypes via annotations from base type (recursively)
        _collectAndResolve(ac, rootType, config, ai, collected);

        return new ArrayList<NamedType>(collected.values());
    }
