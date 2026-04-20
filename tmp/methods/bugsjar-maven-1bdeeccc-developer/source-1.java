    public ArtifactResolutionResult collect( Set artifacts, Artifact originatingArtifact, Map managedVersions,
                                             ArtifactRepository localRepository, List remoteRepositories,
                                             ArtifactMetadataSource source, ArtifactFilter filter, List listeners )
        throws ArtifactResolutionException
    {
        Map resolvedArtifacts = new HashMap();

        ResolutionNode root = new ResolutionNode( originatingArtifact, remoteRepositories );

        root.addDependencies( artifacts, remoteRepositories, filter );

        recurse( root, resolvedArtifacts, managedVersions, localRepository, remoteRepositories, source, filter,
                 listeners );

        Set set = new HashSet();

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
                        // If it was optional, we don't add it or its children, just allow the update of the version and scope
                        if ( !artifact.isOptional() )
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
    private void recurse( ResolutionNode node, Map resolvedArtifacts, Map managedVersions,
                          ArtifactRepository localRepository, List remoteRepositories, ArtifactMetadataSource source,
                          ArtifactFilter filter, List listeners )
        throws CyclicDependencyException, ArtifactResolutionException, OverConstrainedVersionException
    {
        fireEvent( ResolutionListener.TEST_ARTIFACT, listeners, node );

        // TODO: use as a conflict resolver
        Object key = node.getKey();
        if ( managedVersions.containsKey( key ) )
        {
            Artifact artifact = (Artifact) managedVersions.get( key );

            fireEvent( ResolutionListener.MANAGE_ARTIFACT, listeners, node, artifact );

            if ( artifact.getVersion() != null )
            {
                node.getArtifact().setVersion( artifact.getVersion() );
            }
            if ( artifact.getScope() != null )
            {
                node.getArtifact().setScope( artifact.getScope() );
            }
        }

        List previousNodes = (List) resolvedArtifacts.get( key );
        if ( previousNodes != null )
        {
            for ( Iterator i = previousNodes.iterator(); i.hasNext(); )
            {
                ResolutionNode previous = (ResolutionNode) i.next();

                if ( previous.isActive() )
                {
                    // Version mediation
                    VersionRange previousRange = previous.getArtifact().getVersionRange();
                    VersionRange currentRange = node.getArtifact().getVersionRange();

                    // TODO: why do we force the version on it? what if they don't match?
                    if ( previousRange == null )
                    {
                        // version was already resolved
                        node.getArtifact().setVersion( previous.getArtifact().getVersion() );
                    }
                    else if ( currentRange == null )
                    {
                        // version was already resolved
                        previous.getArtifact().setVersion( node.getArtifact().getVersion() );
                    }
                    else
                    {
                        // TODO: shouldn't need to double up on this work, only done for simplicity of handling recommended
                        // version but the restriction is identical
                        VersionRange newRange = previousRange.restrict( currentRange );
                        // TODO: ick. this forces the OCE that should have come from the previous call. It is still correct
                        if ( newRange.isSelectedVersionKnown( previous.getArtifact() ) )
                        {
                            fireEvent( ResolutionListener.RESTRICT_RANGE, listeners, node, previous.getArtifact(),
                                       newRange );
                        }
                        previous.getArtifact().setVersionRange( newRange );
                        node.getArtifact().setVersionRange( currentRange.restrict( previousRange ) );

                        //Select an appropriate available version from the (now restricted) range
                        //Note this version was selected before to get the appropriate POM
                        //But it was reset by the call to setVersionRange on restricting the version
                        ResolutionNode[] resetNodes = {previous, node};
                        for ( int j = 0; j < 2; j++ )
                        {
                            Artifact resetArtifact = resetNodes[j].getArtifact();
                            if ( resetArtifact.getVersion() == null && resetArtifact.getVersionRange() != null &&
                                resetArtifact.getAvailableVersions() != null )
                            {

                                resetArtifact.selectVersion( resetArtifact.getVersionRange().matchVersion(
                                    resetArtifact.getAvailableVersions() ).toString() );
                                fireEvent( ResolutionListener.SELECT_VERSION_FROM_RANGE, listeners, resetNodes[j] );
                            }
                        }
                    }

                    // Conflict Resolution
                    // TODO: use as conflict resolver(s), chain

                    // TODO: should this be part of mediation?
                    // previous one is more dominant
                    if ( previous.getDepth() <= node.getDepth() )
                    {
                        checkScopeUpdate( node, previous, listeners );
                    }
                    else
                    {
                        checkScopeUpdate( previous, node, listeners );
                    }

                    if ( previous.getDepth() <= node.getDepth() )
                    {
                        // previous was nearer
                        fireEvent( ResolutionListener.OMIT_FOR_NEARER, listeners, node, previous.getArtifact() );
                        node.disable();
                    }
                    else
                    {
                        fireEvent( ResolutionListener.OMIT_FOR_NEARER, listeners, previous, node.getArtifact() );
                        previous.disable();
                    }
                }
            }
        }
        else
        {
            previousNodes = new ArrayList();
            resolvedArtifacts.put( key, previousNodes );
        }
        previousNodes.add( node );

        fireEvent( ResolutionListener.INCLUDE_ARTIFACT, listeners, node );

        if ( node.isActive() )
        {
            fireEvent( ResolutionListener.PROCESS_CHILDREN, listeners, node );

            for ( Iterator i = node.getChildrenIterator(); i.hasNext(); )
            {
                ResolutionNode child = (ResolutionNode) i.next();
                // We leave in optional ones, but don't pick up its dependencies
                if ( !child.isResolved() && !child.getArtifact().isOptional() )
                {
                    Artifact artifact = child.getArtifact();
                    try
                    {
                        if ( artifact.getVersion() == null )
                        {
                            // set the recommended version
                            // TODO: maybe its better to just pass the range through to retrieval and use a transformation?
                            ArtifactVersion version;
                            if ( !artifact.isSelectedVersionKnown() )
                            {
                                List versions = artifact.getAvailableVersions();
                                if ( versions == null )
                                {
                                    versions = source.retrieveAvailableVersions( artifact, localRepository,
                                                                                 remoteRepositories );
                                    artifact.setAvailableVersions( versions );
                                }

                                VersionRange versionRange = artifact.getVersionRange();

                                version = versionRange.matchVersion( versions );

                                if ( version == null )
                                {
                                    if ( versions.isEmpty() )
                                    {
                                        throw new OverConstrainedVersionException(
                                            "No versions are present in the repository for the artifact with a range " +
                                                versionRange, artifact, remoteRepositories );
                                    }
                                    else
                                    {
                                        throw new OverConstrainedVersionException( "Couldn't find a version in " +
                                            versions + " to match range " + versionRange, artifact,
                                                                                          remoteRepositories );
                                    }
                                }
                            }
                            else
                            {
                                version = artifact.getSelectedVersion();
                            }

                            artifact.selectVersion( version.toString() );
                            fireEvent( ResolutionListener.SELECT_VERSION_FROM_RANGE, listeners, child );
                        }

                        ResolutionGroup rGroup = source.retrieve( artifact, localRepository, remoteRepositories );

                        //TODO might be better to have source.retreive() throw a specific exception for this situation
                        //and catch here rather than have it return null
                        if ( rGroup == null )
                        {
                            //relocated dependency artifact is declared excluded, no need to add and recurse further
                            continue;
                        }

                        child.addDependencies( rGroup.getArtifacts(), rGroup.getResolutionRepositories(), filter );
                    }
                    catch ( CyclicDependencyException e )
                    {
                        // would like to throw this, but we have crappy stuff in the repo

                        fireEvent( ResolutionListener.OMIT_FOR_CYCLE, listeners,
                                   new ResolutionNode( e.getArtifact(), remoteRepositories, child ) );
                    }
                    catch ( ArtifactMetadataRetrievalException e )
                    {
                        artifact.setDependencyTrail( node.getDependencyTrail() );
                        throw new ArtifactResolutionException(
                            "Unable to get dependency information: " + e.getMessage(), artifact, remoteRepositories,
                            e );
                    }

                    recurse( child, resolvedArtifacts, managedVersions, localRepository, remoteRepositories, source,
                             filter, listeners );
                }
            }

            fireEvent( ResolutionListener.FINISH_PROCESSING_CHILDREN, listeners, node );
        }
    }
    public boolean isResolved()
    {
        return children != null;
    }
