    private ManagedVersionMap getManagedVersionsMap( Artifact originatingArtifact, Map managedVersions )
    {
        ManagedVersionMap versionMap;
        if ( managedVersions != null && managedVersions instanceof ManagedVersionMap )
        {
            versionMap = (ManagedVersionMap) managedVersions;
        }
        else
        {
            versionMap = new ManagedVersionMap( managedVersions );
        }

        /* remove the originating artifact if it is also in managed versions to avoid being modified during resolution */
        Artifact managedOriginatingArtifact = (Artifact) versionMap.get( originatingArtifact.getDependencyConflictId() );
        if ( managedOriginatingArtifact != null )
        {
            String managedVersion = managedOriginatingArtifact.getVersion();
            String version = originatingArtifact.getVersion();
            if ( !managedVersion.equals( version ) )
            {
                // TODO we probably want to warn the user that he is building and artifact with a
                // different version than in dependencyManagement
                if ( managedVersions instanceof ManagedVersionMap )
                {
                    /* avoid modifying the managedVersions parameter creating a new map */
                    versionMap = new ManagedVersionMap( managedVersions );
                }
                versionMap.remove( originatingArtifact.getDependencyConflictId() );
            }
        }

        return versionMap;
    }
    public ArtifactResolutionResult collect( Set artifacts, Artifact originatingArtifact, Map managedVersions,
                                             ArtifactRepository localRepository, List remoteRepositories,
                                             ArtifactMetadataSource source, ArtifactFilter filter, List listeners )
        throws ArtifactResolutionException
    {
        Map resolvedArtifacts = new LinkedHashMap();

        ResolutionNode root = new ResolutionNode( originatingArtifact, remoteRepositories );

        root.addDependencies( artifacts, remoteRepositories, filter );

        ManagedVersionMap versionMap = getManagedVersionsMap( originatingArtifact, managedVersions );

        recurse( root, resolvedArtifacts, versionMap, localRepository, remoteRepositories, source, filter,
                 listeners );

        Set set = new LinkedHashSet();

        for ( Iterator i = resolvedArtifacts.values().iterator(); i.hasNext(); )
        {
            List nodes = (List) i.next();
            for ( Iterator j = nodes.iterator(); j.hasNext(); )
            {
                ResolutionNode node = (ResolutionNode) j.next();
                if ( !node.equals( root ) && node.isActive() )
                {
                    Artifact artifact = node.getArtifact();

                    if ( node.filterTrail( filter ) )
                    {
                        // If it was optional and not a direct dependency,
                        // we don't add it or its children, just allow the update of the version and scope
                        if ( node.isChildOfRootNode() || !artifact.isOptional() )
                        {
                            artifact.setDependencyTrail( node.getDependencyTrail() );

                            set.add( node );
                        }
                    }
                }
            }
        }

        ArtifactResolutionResult result = new ArtifactResolutionResult();
        result.setArtifactResolutionNodes( set );
        return result;
    }
