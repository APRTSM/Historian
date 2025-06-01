    private static void copyDefaultValues(QValue[] qValues, NodeBuilder builder,
            NameMapper nameMapper) throws RepositoryException {
        if (qValues.length == 0) {
            builder.setProperty(JCR_DEFAULTVALUES, Collections.<String>emptyList(), STRINGS);
        } else {
            int type = qValues[0].getType();
            switch (type) {
                case PropertyType.STRING:
                    List<String> strings = newArrayListWithCapacity(qValues.length);
                    for (QValue qValue : qValues) {
                        strings.add(qValue.getString());
                    }
                    builder.setProperty(createProperty(JCR_DEFAULTVALUES, strings, STRINGS));
                    return;
                case PropertyType.LONG:
                    List<Long> longs = newArrayListWithCapacity(qValues.length);
                    for (QValue qValue : qValues) {
                        longs.add(qValue.getLong());
                    }
                    builder.setProperty(createProperty(JCR_DEFAULTVALUES, longs, LONGS));
                    return;
                case PropertyType.DOUBLE:
                    List<Double> doubles = newArrayListWithCapacity(qValues.length);
                    for (QValue qValue : qValues) {
                        doubles.add(qValue.getDouble());
                    }
                    builder.setProperty(createProperty(JCR_DEFAULTVALUES, doubles, DOUBLES));
                    return;
                case PropertyType.BOOLEAN:
                    List<Boolean> booleans = Lists.newArrayListWithCapacity(qValues.length);
                    for (QValue qValue : qValues) {
                        booleans.add(qValue.getBoolean());
                    }
                    builder.setProperty(createProperty(JCR_DEFAULTVALUES, booleans, BOOLEANS));
                    return;
                case PropertyType.NAME:
                    List<String> names = Lists.newArrayListWithCapacity(qValues.length);
                    for (QValue qValue : qValues) {
                        names.add(nameMapper.getOakName(qValue.getName().toString()));
                    }
                    builder.setProperty(createProperty(JCR_DEFAULTVALUES, names, NAMES));
                    return;
                case PropertyType.PATH:
                    List<String> paths = Lists.newArrayListWithCapacity(qValues.length);
                    for (QValue qValue : qValues) {
                        paths.add(getOakPath(qValue.getPath(), nameMapper));
                    }
                    builder.setProperty(createProperty(JCR_DEFAULTVALUES, paths, PATHS));
                    return;
                case PropertyType.DECIMAL:
                    List<BigDecimal> decimals = Lists.newArrayListWithCapacity(qValues.length);
                    for (QValue qValue : qValues) {
                        decimals.add(qValue.getDecimal());
                    }
                    builder.setProperty(createProperty(JCR_DEFAULTVALUES, decimals, DECIMALS));
                    return;
                case PropertyType.DATE:
                case PropertyType.URI:
                    List<String> values = newArrayListWithCapacity(qValues.length);
                    for (QValue qValue : qValues) {
                        values.add(qValue.getString());
                    }
                    builder.setProperty(createProperty(JCR_DEFAULTVALUES, values, Type.fromTag(type, true)));
                    return;
                default:
                    throw new UnsupportedRepositoryOperationException(
                            "Cannot copy default value of type " + Type.fromTag(type, true));
            }
        }
    }
    private static String getOakPath(Path path, NameMapper nameMapper)
            throws RepositoryException {
        StringBuilder oakPath = new StringBuilder();
        String sep = "";
        for (Element element: path.getElements()) {
            if (element.denotesRoot()) {
                oakPath.append('/');
                continue;
            } else if (element.denotesName()) {
                oakPath.append(sep).append(nameMapper.getOakName(element.getString()));
            } else if (element.denotesCurrent()) {
                oakPath.append(sep).append('.');
            } else if (element.denotesParent()) {
                oakPath.append(sep).append("..");
            } else {
                throw new UnsupportedRepositoryOperationException("Cannot copy default value " + path);
            }
            sep = "/";
        }
        return oakPath.toString();
    }
    private void copyNodeTypes(NodeBuilder root, Map<String, String> prefixToUri)
            throws RepositoryException {
        NodeTypeRegistry sourceRegistry = source.getNodeTypeRegistry();
        NodeBuilder system = root.child(JCR_SYSTEM);
        NodeBuilder types = system.child(JCR_NODE_TYPES);

        logger.info("Copying registered node types");
        for (Name name : sourceRegistry.getRegisteredNodeTypes()) {
            String oakName = getOakName(name);
            // skip built-in nodetypes (OAK-1235)
            if (!types.hasChildNode(oakName)) {
                QNodeTypeDefinition def = sourceRegistry.getNodeTypeDef(name);
                NodeBuilder type = types.child(oakName);
                copyNodeType(def, type, prefixToUri);
            }
        }
    }
    private void copyPropertyDefinition(
            QPropertyDefinition def, NodeBuilder builder, Map<String, String> prefixToUri)
            throws RepositoryException {
        builder.setProperty(JCR_PRIMARYTYPE, NT_PROPERTYDEFINITION, NAME);

        copyItemDefinition(def, builder);

        // - jcr:requiredType (STRING) protected mandatory
        //   < 'STRING', 'URI', 'BINARY', 'LONG', 'DOUBLE',
        //     'DECIMAL', 'BOOLEAN', 'DATE', 'NAME', 'PATH',
        //     'REFERENCE', 'WEAKREFERENCE', 'UNDEFINED'
        builder.setProperty(
                JCR_REQUIREDTYPE,
                Type.fromTag(def.getRequiredType(), false).toString());
        // - jcr:valueConstraints (STRING) protected multiple
        QValueConstraint[] constraints = def.getValueConstraints();
        if (constraints != null && constraints.length > 0) {
            List<String> strings = newArrayListWithCapacity(constraints.length);
            for (QValueConstraint constraint : constraints) {
                strings.add(constraint.getString());
            }
            builder.setProperty(JCR_VALUECONSTRAINTS, strings, STRINGS);
        }
        // - jcr:defaultValues (UNDEFINED) protected multiple
        QValue[] qValues = def.getDefaultValues();
        if (qValues != null) {
            copyDefaultValues(qValues, builder, new GlobalNameMapper(prefixToUri));
        }
        // - jcr:multiple (BOOLEAN) protected mandatory
        builder.setProperty(JCR_MULTIPLE, def.isMultiple());
        // - jcr:availableQueryOperators (NAME) protected mandatory multiple
        List<String> operators = asList(def.getAvailableQueryOperators());
        builder.setProperty(JCR_AVAILABLE_QUERY_OPERATORS, operators, NAMES);
        // - jcr:isFullTextSearchable (BOOLEAN) protected mandatory
        builder.setProperty(
                JCR_IS_FULLTEXT_SEARCHABLE, def.isFullTextSearchable());
        // - jcr:isQueryOrderable (BOOLEAN) protected mandatory
        builder.setProperty(JCR_IS_QUERY_ORDERABLE, def.isQueryOrderable());
    }
    private void copyNodeType(
            QNodeTypeDefinition def, NodeBuilder builder, Map<String, String> prefixToUri)
            throws RepositoryException {
        builder.setProperty(JCR_PRIMARYTYPE, NT_NODETYPE, NAME);

        // - jcr:nodeTypeName (NAME) protected mandatory
        builder.setProperty(JCR_NODETYPENAME, getOakName(def.getName()), NAME);
        // - jcr:supertypes (NAME) protected multiple
        Name[] supertypes = def.getSupertypes();
        if (supertypes != null && supertypes.length > 0) {
            List<String> names = newArrayListWithCapacity(supertypes.length);
            for (Name supertype : supertypes) {
                names.add(getOakName(supertype));
            }
            builder.setProperty(JCR_SUPERTYPES, names, NAMES);
        }
        // - jcr:isAbstract (BOOLEAN) protected mandatory
        builder.setProperty(JCR_IS_ABSTRACT, def.isAbstract());
        // - jcr:isQueryable (BOOLEAN) protected mandatory
        builder.setProperty(JCR_IS_QUERYABLE, def.isQueryable());
        // - jcr:isMixin (BOOLEAN) protected mandatory
        builder.setProperty(JCR_ISMIXIN, def.isMixin());
        // - jcr:hasOrderableChildNodes (BOOLEAN) protected mandatory
        builder.setProperty(
                JCR_HASORDERABLECHILDNODES, def.hasOrderableChildNodes());
        // - jcr:primaryItemName (NAME) protected
        Name primary = def.getPrimaryItemName();
        if (primary != null) {
            builder.setProperty(
                    JCR_PRIMARYITEMNAME, getOakName(primary), NAME);
        }

        // + jcr:propertyDefinition (nt:propertyDefinition) = nt:propertyDefinition protected sns
        QPropertyDefinition[] properties = def.getPropertyDefs();
        for (int i = 0; i < properties.length; i++) {
            String name = JCR_PROPERTYDEFINITION + '[' + (i + 1) + ']';
            copyPropertyDefinition(properties[i], builder.child(name), prefixToUri);
        }

        // + jcr:childNodeDefinition (nt:childNodeDefinition) = nt:childNodeDefinition protected sns
        QNodeDefinition[] childNodes = def.getChildNodeDefs();
        for (int i = 0; i < childNodes.length; i++) {
            String name = JCR_CHILDNODEDEFINITION + '[' + (i + 1) + ']';
            copyChildNodeDefinition(childNodes[i], builder.child(name));
        }
    }
    public void copy(RepositoryInitializer initializer) throws RepositoryException {
        RepositoryConfig config = source.getRepositoryConfig();
        logger.info(
                "Copying repository content from {} to Oak", config.getHomeDir());
        try {
            NodeBuilder builder = target.getRoot().builder();

            String workspace =
                    source.getRepositoryConfig().getDefaultWorkspaceName();
            SecurityProviderImpl security = new SecurityProviderImpl(
                    mapSecurityConfig(config.getSecurityConfig()));

            // init target repository first
            new InitialContent().initialize(builder);
            if (initializer != null) {
                initializer.initialize(builder);
            }
            for (SecurityConfiguration sc : security.getConfigurations()) {
                sc.getWorkspaceInitializer().initialize(builder, workspace);
            }

            HashBiMap<String, String> uriToPrefix = HashBiMap.create();
            Map<Integer, String> idxToPrefix = newHashMap();
            copyNamespaces(builder, uriToPrefix, idxToPrefix);
            copyNodeTypes(builder, uriToPrefix.inverse());
            copyPrivileges(builder);

            NodeState root = builder.getNodeState();
            copyVersionStore(builder, root, uriToPrefix, idxToPrefix);
            copyWorkspace(builder, root, workspace, uriToPrefix, idxToPrefix);

            logger.info("Applying default commit hooks");
            // TODO: default hooks?
            List<CommitHook> hooks = newArrayList();

            UserConfiguration userConf =
                    security.getConfiguration(UserConfiguration.class);
            String groupsPath = userConf.getParameters().getConfigValue(
                    UserConstants.PARAM_GROUP_PATH,
                    UserConstants.DEFAULT_GROUP_PATH);

            // hooks specific to the upgrade, need to run first
            hooks.add(new EditorHook(new CompositeEditorProvider(
                    new RestrictionEditorProvider(),
                    new GroupEditorProvider(groupsPath))));

            // security-related hooks
            for (SecurityConfiguration sc : security.getConfigurations()) {
                hooks.addAll(sc.getCommitHooks(workspace));
            }

            // type validation, reference and indexing hooks
            hooks.add(new EditorHook(new CompositeEditorProvider(
                            new TypeEditorProvider(false),
                            new IndexUpdateProvider(new CompositeIndexEditorProvider(
                                    new ReferenceEditorProvider(),
                                    new PropertyIndexEditorProvider())))));

            target.merge(builder, CompositeHook.compose(hooks), CommitInfo.EMPTY);
        } catch (Exception e) {
            throw new RepositoryException("Failed to copy content", e);
        }
    }
