    public List<Artifact> resolve( Plugin plugin, Artifact pluginArtifact, ArtifactResolutionRequest request,
                                   ArtifactFilter dependencyFilter )
        throws PluginResolutionException
    {
        if ( pluginArtifact == null )
        {
            pluginArtifact = repositorySystem.createPluginArtifact( plugin );
        }

        Set<Artifact> overrideArtifacts = new LinkedHashSet<Artifact>();
        for ( Dependency dependency : plugin.getDependencies() )
        {
	          if ( !Artifact.SCOPE_SYSTEM.equals( dependency.getScope() ) )
	          {
	              dependency.setScope( Artifact.SCOPE_RUNTIME );
	          }
            overrideArtifacts.add( repositorySystem.createDependencyArtifact( dependency ) );
        }

        ArtifactFilter collectionFilter = new ScopeArtifactFilter( Artifact.SCOPE_RUNTIME_PLUS_SYSTEM );

        ArtifactFilter resolutionFilter = artifactFilterManager.getCoreArtifactFilter();

        PluginDependencyResolutionListener listener = new PluginDependencyResolutionListener( resolutionFilter );

        if ( dependencyFilter != null )
        {
            resolutionFilter = new AndArtifactFilter( Arrays.asList( resolutionFilter, dependencyFilter ) );
        }

        request.setArtifact( pluginArtifact );
        request.setArtifactDependencies( overrideArtifacts );
        request.setCollectionFilter( collectionFilter );
        request.setResolutionFilter( resolutionFilter );
        request.setResolveRoot( true );
        request.setResolveTransitively( true );
        request.addListener( listener );

        ArtifactResolutionResult result = repositorySystem.resolve( request );

        try
        {
            resolutionErrorHandler.throwErrors( request, result );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new PluginResolutionException( plugin, e );
        }

        List<Artifact> pluginArtifacts = new ArrayList<Artifact>( result.getArtifacts() );

        listener.removeBannedDependencies( pluginArtifacts );

        addPlexusUtils( pluginArtifacts, plugin, request );

        return pluginArtifacts;
    }
