    private JackrabbitNodeState(
            JackrabbitNodeState parent, String name, NodePropBundle bundle) {
        this.parent = parent;
        this.name = name;
        this.path = null;
        this.loader = parent.loader;
        this.isReferenceable = parent.isReferenceable;
        this.isOrderable = parent.isOrderable;
        this.uriToPrefix = parent.uriToPrefix;
        this.nodes = createNodes(bundle);
        this.properties = createProperties(bundle);
        this.useBinaryReferences = parent.useBinaryReferences;
        logNewNode(this);
    }
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
        this.uriToPrefix = uriToPrefix;
        try {
            NodePropBundle bundle = loader.loadBundle(id);
            this.nodes = createNodes(bundle);
            this.properties = createProperties(bundle);
        } catch (ItemStateException e) {
            throw new IllegalStateException("Unable to access node " + id, e);
        }
        this.useBinaryReferences = useBinaryReferences;
        logNewNode(this);
    }
    public Map<String, PropertyState> createProperties(NodePropBundle bundle) {
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

        if (isOrderable.apply(primary, mixins)) {
            properties.put(OAK_CHILD_ORDER, PropertyStates.createProperty(
                    OAK_CHILD_ORDER, nodes.keySet(), Type.NAMES));
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
