    public synchronized void setupPluginRealm( PluginDescriptor pluginDescriptor, MavenSession session,
                                               ClassLoader parent, List<String> imports, DependencyFilter filter )
        throws PluginResolutionException, PluginContainerException
    {
        Plugin plugin = pluginDescriptor.getPlugin();

        MavenProject project = session.getCurrentProject();

        Map<String, ClassLoader> foreignImports = calcImports( project, parent, imports );

        PluginRealmCache.Key cacheKey =
            pluginRealmCache.createKey( plugin, parent, foreignImports, filter, project.getRemotePluginRepositories(),
                                        session.getRepositorySession() );

        PluginRealmCache.CacheRecord cacheRecord = pluginRealmCache.get( cacheKey );

        if ( cacheRecord != null )
        {
            pluginDescriptor.setClassRealm( cacheRecord.realm );
            pluginDescriptor.setArtifacts( new ArrayList<Artifact>( cacheRecord.artifacts ) );
            for ( ComponentDescriptor<?> componentDescriptor : pluginDescriptor.getComponents() )
            {
                componentDescriptor.setRealm( cacheRecord.realm );
            }
        }
        else
        {
            createPluginRealm( pluginDescriptor, session, parent, foreignImports, filter );

            cacheRecord =
                pluginRealmCache.put( cacheKey, pluginDescriptor.getClassRealm(), pluginDescriptor.getArtifacts() );
        }

        pluginRealmCache.register( project, cacheRecord );
    }
