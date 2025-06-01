    private void createPluginRealm( PluginDescriptor pluginDescriptor, MavenSession session, ClassLoader parent,
                                    Map<String, ClassLoader> foreignImports, DependencyFilter filter )
        throws PluginResolutionException, PluginContainerException
    {
        Plugin plugin = pluginDescriptor.getPlugin();

        if ( plugin == null )
        {
            throw new IllegalArgumentException( "incomplete plugin descriptor, plugin missing" );
        }

        Artifact pluginArtifact = pluginDescriptor.getPluginArtifact();

        if ( pluginArtifact == null )
        {
            throw new IllegalArgumentException( "incomplete plugin descriptor, plugin artifact missing" );
        }

        MavenProject project = session.getCurrentProject();

        final ClassRealm pluginRealm;
        final List<Artifact> pluginArtifacts;

        RepositorySystemSession repositorySession = session.getRepositorySession();
        if ( plugin.isExtensions() )
        {
            // TODO discover components in #setupExtensionsRealm

            ExtensionRealmCache.CacheRecord extensionRecord;
            try
            {
                extensionRecord = setupExtensionsRealm( project, plugin, repositorySession );
            }
            catch ( PluginManagerException e )
            {
                // extensions realm is expected to be fully setup at this point
                // any exception means a problem in maven code, not a user error
                throw new IllegalStateException( e );
            }

            pluginRealm = extensionRecord.realm;
            pluginArtifacts = extensionRecord.artifacts;
        }
        else
        {
            DependencyFilter dependencyFilter = project.getExtensionDependencyFilter();
            dependencyFilter = AndDependencyFilter.newInstance( dependencyFilter, filter );

            DependencyNode root =
                pluginDependenciesResolver.resolve( plugin, RepositoryUtils.toArtifact( pluginArtifact ),
                                                    dependencyFilter, project.getRemotePluginRepositories(),
                                                    repositorySession );

            PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
            root.accept( nlg );

            pluginArtifacts = toMavenArtifacts( root, nlg );

            pluginRealm =
                classRealmManager.createPluginRealm( plugin, parent, null, foreignImports,
                                                     toAetherArtifacts( pluginArtifacts ) );

            discoverPluginComponents( pluginRealm, plugin, pluginDescriptor );
        }

        pluginDescriptor.setClassRealm( pluginRealm );
        pluginDescriptor.setArtifacts( pluginArtifacts );
    }
    public ExtensionRealmCache.CacheRecord setupExtensionsRealm( MavenProject project, Plugin plugin,
                                                                 RepositorySystemSession session )
        throws PluginManagerException
    {
        @SuppressWarnings( "unchecked" )
        Map<String, ExtensionRealmCache.CacheRecord> pluginRealms =
            (Map<String, ExtensionRealmCache.CacheRecord>) project.getContextValue( KEY_EXTENSIONS_REALMS );
        if ( pluginRealms == null )
        {
            pluginRealms = new HashMap<String, ExtensionRealmCache.CacheRecord>();
            project.setContextValue( KEY_EXTENSIONS_REALMS, pluginRealms );
        }

        final String pluginKey = plugin.getId();

        ExtensionRealmCache.CacheRecord extensionRecord = pluginRealms.get( pluginKey );
        if ( extensionRecord != null )
        {
            return extensionRecord;
        }

        final List<RemoteRepository> repositories = project.getRemotePluginRepositories();

        // resolve plugin version as necessary
        if ( plugin.getVersion() == null )
        {
            PluginVersionRequest versionRequest = new DefaultPluginVersionRequest( plugin, session, repositories );
            try
            {
                plugin.setVersion( pluginVersionResolver.resolve( versionRequest ).getVersion() );
            }
            catch ( PluginVersionResolutionException e )
            {
                throw new PluginManagerException( plugin, e.getMessage(), e );
            }
        }

        // resolve plugin artifacts
        List<Artifact> artifacts;
        PluginArtifactsCache.Key cacheKey = pluginArtifactsCache.createKey( plugin, null, repositories, session );
        PluginArtifactsCache.CacheRecord recordArtifacts;
        try
        {
            recordArtifacts = pluginArtifactsCache.get( cacheKey );
        }
        catch ( PluginResolutionException e )
        {
            throw new PluginManagerException( plugin, e.getMessage(), e );
        }
        if ( recordArtifacts != null )
        {
            artifacts = recordArtifacts.artifacts;
        }
        else
        {
            try
            {
                artifacts = resolveExtensionArtifacts( plugin, repositories, session );
                recordArtifacts = pluginArtifactsCache.put( cacheKey, artifacts );
            }
            catch ( PluginResolutionException e )
            {
                pluginArtifactsCache.put( cacheKey, e );
                pluginArtifactsCache.register( project, cacheKey, recordArtifacts );
                throw new PluginManagerException( plugin, e.getMessage(), e );
            }
        }
        pluginArtifactsCache.register( project, cacheKey, recordArtifacts );

        // create and cache extensions realms
        final ExtensionRealmCache.Key extensionKey = extensionRealmCache.createKey( artifacts );
        extensionRecord = extensionRealmCache.get( extensionKey );
        if ( extensionRecord == null )
        {
            ClassRealm extensionRealm = classRealmManager.createExtensionRealm( plugin, toAetherArtifacts( artifacts ) );

            PluginDescriptor pluginDescriptor = null;
            if ( plugin.isExtensions() && !artifacts.isEmpty() )
            {
                // ignore plugin descriptor parsing errors at this point
                // these errors will reported during calculation of project build execution plan
                try
                {
                    pluginDescriptor = extractPluginDescriptor( artifacts.get( 0 ), plugin );
                }
                catch ( PluginDescriptorParsingException e )
                {
                    // ignore, see above
                }
                catch ( InvalidPluginDescriptorException e )
                {
                    // ignore, see above
                }
            }

            discoverPluginComponents( extensionRealm, plugin, pluginDescriptor );

            ExtensionDescriptor extensionDescriptor = null;
            Artifact extensionArtifact = artifacts.get( 0 );
            try
            {
                extensionDescriptor = extensionDescriptorBuilder.build( extensionArtifact.getFile() );
            }
            catch ( IOException e )
            {
                String message = "Invalid extension descriptor for " + plugin.getId() + ": " + e.getMessage();
                if ( logger.isDebugEnabled() )
                {
                    logger.error( message, e );
                }
                else
                {
                    logger.error( message );
                }
            }
            extensionRecord = extensionRealmCache.put( extensionKey, extensionRealm, extensionDescriptor, artifacts );
        }
        extensionRealmCache.register( project, extensionKey, extensionRecord );
        pluginRealms.put( pluginKey, extensionRecord );

        return extensionRecord;
    }
