    JackrabbitNodeState(
            PersistenceManager source, NodeState root,
            Map<String, String> uriToPrefix, NodeId id, String path,
            boolean useBinaryReferences) {
        this.parent = null;
        this.name = null;
        this.path = path;
        this.loader = new BundleLoader(source);
        this.isReferenceable = new TypePredicate(root, MIX_REFERENCEABLE);
        this.isOrderable = TypePredicate.isOrderable(root);
        this.isFrozenNode = new TypePredicate(root, NT_FROZENNODE);
        this.uriToPrefix = uriToPrefix;
        try {
            NodePropBundle bundle = loader.loadBundle(id);
            this.properties = createProperties(bundle);
            this.nodes = createNodes(bundle);
            setChildOrder();
        } catch (ItemStateException e) {
            throw new IllegalStateException("Unable to access node " + id, e);
        }
        this.useBinaryReferences = useBinaryReferences;
        logNewNode(this);
    }
    private Map<String, PropertyState> createProperties(NodePropBundle bundle) {
        Map<String, PropertyState> properties = newHashMap();

        String primary;
        if (bundle.getNodeTypeName() != null) {
            primary = createName(bundle.getNodeTypeName());
        } else {
            warn("Missing primary node type; defaulting to nt:unstructured");
            primary = NT_UNSTRUCTURED;
        }
        properties.put(JCR_PRIMARYTYPE, PropertyStates.createProperty(
                JCR_PRIMARYTYPE, primary, Type.NAME));

        Set<String> mixins = newLinkedHashSet();
        if (bundle.getMixinTypeNames() != null) {
            for (Name mixin : bundle.getMixinTypeNames()) {
                mixins.add(createName(mixin));
            }
        }
        if (!mixins.isEmpty()) {
            properties.put(JCR_MIXINTYPES, PropertyStates.createProperty(
                    JCR_MIXINTYPES, mixins, Type.NAMES));
        }

        if (bundle.isReferenceable()
                || isReferenceable.apply(primary, mixins)) {
            properties.put(JCR_UUID, PropertyStates.createProperty(
                    JCR_UUID, bundle.getId().toString()));
        }

        for (PropertyEntry property : bundle.getPropertyEntries()) {
            String name = createName(property.getName());
            try {
                int type = property.getType();
                if (property.isMultiValued()) {
                    properties.put(name, createProperty(
                            name, type, property.getValues()));
                } else {
                    properties.put(name, createProperty(
                            name, type, property.getValues()[0]));
                }
            } catch (Exception e) {
                warn("Skipping broken property entry " + name, e);
            }
        }

        return properties;
    }
    private void fixFrozenUuid() {
        // OAK-1789: Convert the jcr:frozenUuid of a non-referenceable
        // frozen node from UUID to a path identifier
        PropertyState frozenUuid = properties.get(JCR_FROZENUUID);
        if (frozenUuid != null
                && frozenUuid.getType() == STRING
                && isFrozenNode.apply(this)) {
            String frozenPrimary = NT_BASE;
            Set<String> frozenMixins = newHashSet();

            PropertyState property = properties.get(JCR_FROZENPRIMARYTYPE);
            if (property != null && property.getType() == NAME) {
                frozenPrimary = property.getValue(NAME);
            }
            property = properties.get(JCR_FROZENMIXINTYPES);
            if (property != null && property.getType() == NAMES) {
                addAll(frozenMixins, property.getValue(NAMES));
            }

            if (!isReferenceable.apply(frozenPrimary, frozenMixins)) {
                frozenUuid = PropertyStates.createProperty(
                        JCR_FROZENUUID,
                        parent.getString(JCR_FROZENUUID) + "/" + name);
                properties.put(JCR_FROZENUUID, frozenUuid);
            }
        }
    }
    private JackrabbitNodeState(
            JackrabbitNodeState parent, String name, NodePropBundle bundle) {
        this.parent = parent;
        this.name = name;
        this.path = null;
        this.loader = parent.loader;
        this.isReferenceable = parent.isReferenceable;
        this.isOrderable = parent.isOrderable;
        this.isFrozenNode = parent.isFrozenNode;
        this.uriToPrefix = parent.uriToPrefix;
        this.properties = createProperties(bundle);
        this.nodes = createNodes(bundle);
        setChildOrder();
        fixFrozenUuid();
        this.useBinaryReferences = parent.useBinaryReferences;
        logNewNode(this);
    }
    private void setChildOrder() {
        if (isOrderable.apply(this)) {
            properties.put(OAK_CHILD_ORDER, PropertyStates.createProperty(
                    OAK_CHILD_ORDER, nodes.keySet(), Type.NAMES));
        }
    }
