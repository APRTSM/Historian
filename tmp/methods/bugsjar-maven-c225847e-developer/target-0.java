    private Model loadPom( RepositorySystemSession session, ArtifactDescriptorRequest request,
                           ArtifactDescriptorResult result )
        throws ArtifactDescriptorException
    {
        RequestTrace trace = RequestTrace.newChild( request.getTrace(), request );

        Set<String> visited = new LinkedHashSet<String>();
        for ( Artifact artifact = request.getArtifact();; )
        {
            Artifact pomArtifact = ArtifactDescriptorUtils.toPomArtifact( artifact );
            try
            {
                VersionRequest versionRequest =
                    new VersionRequest( artifact, request.getRepositories(), request.getRequestContext() );
                versionRequest.setTrace( trace );
                VersionResult versionResult = versionResolver.resolveVersion( session, versionRequest );

                artifact = artifact.setVersion( versionResult.getVersion() );

                versionRequest =
                    new VersionRequest( pomArtifact, request.getRepositories(), request.getRequestContext() );
                versionRequest.setTrace( trace );
                versionResult = versionResolver.resolveVersion( session, versionRequest );

                pomArtifact = pomArtifact.setVersion( versionResult.getVersion() );
            }
            catch ( VersionResolutionException e )
            {
                result.addException( e );
                throw new ArtifactDescriptorException( result );
            }

            if ( !visited.add( artifact.getGroupId() + ':' + artifact.getArtifactId() + ':' + artifact.getBaseVersion() ) )
            {
                RepositoryException exception =
                    new RepositoryException( "Artifact relocations form a cycle: " + visited );
                invalidDescriptor( session, trace, artifact, exception );
                if ( ( getPolicy( session, artifact, request ) & ArtifactDescriptorPolicy.IGNORE_INVALID ) != 0 )
                {
                    return null;
                }
                result.addException( exception );
                throw new ArtifactDescriptorException( result );
            }

            ArtifactResult resolveResult;
            try
            {
                ArtifactRequest resolveRequest =
                    new ArtifactRequest( pomArtifact, request.getRepositories(), request.getRequestContext() );
                resolveRequest.setTrace( trace );
                resolveResult = artifactResolver.resolveArtifact( session, resolveRequest );
                pomArtifact = resolveResult.getArtifact();
                result.setRepository( resolveResult.getRepository() );
            }
            catch ( ArtifactResolutionException e )
            {
                if ( e.getCause() instanceof ArtifactNotFoundException )
                {
                    missingDescriptor( session, trace, artifact, (Exception) e.getCause() );
                    if ( ( getPolicy( session, artifact, request ) & ArtifactDescriptorPolicy.IGNORE_MISSING ) != 0 )
                    {
                        return null;
                    }
                }
                result.addException( e );
                throw new ArtifactDescriptorException( result );
            }

            Model model;
            try
            {
                ModelBuildingRequest modelRequest = new DefaultModelBuildingRequest();
                modelRequest.setValidationLevel( ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL );
                modelRequest.setProcessPlugins( false );
                modelRequest.setTwoPhaseBuilding( false );
                modelRequest.setSystemProperties( toProperties( session.getUserProperties(),
                                                                session.getSystemProperties() ) );
                modelRequest.setModelCache( DefaultModelCache.newInstance( session ) );
                modelRequest.setModelResolver( new DefaultModelResolver( session, trace.newChild( modelRequest ),
                                                                         request.getRequestContext(), artifactResolver,
                                                                         remoteRepositoryManager,
                                                                         request.getRepositories() ) );
                if ( resolveResult.getRepository() instanceof WorkspaceRepository )
                {
                    modelRequest.setPomFile( pomArtifact.getFile() );
                }
                else
                {
                    modelRequest.setModelSource( new FileModelSource( pomArtifact.getFile() ) );
                }

                model = modelBuilder.build( modelRequest ).getEffectiveModel();
            }
            catch ( ModelBuildingException e )
            {
                for ( ModelProblem problem : e.getProblems() )
                {
                    if ( problem.getException() instanceof UnresolvableModelException )
                    {
                        result.addException( problem.getException() );
                        throw new ArtifactDescriptorException( result );
                    }
                }
                invalidDescriptor( session, trace, artifact, e );
                if ( ( getPolicy( session, artifact, request ) & ArtifactDescriptorPolicy.IGNORE_INVALID ) != 0 )
                {
                    return null;
                }
                result.addException( e );
                throw new ArtifactDescriptorException( result );
            }

            Relocation relocation = getRelocation( model );

            if ( relocation != null )
            {
                result.addRelocation( artifact );
                artifact =
                    new RelocatedArtifact( artifact, relocation.getGroupId(), relocation.getArtifactId(),
                                           relocation.getVersion() );
                result.setArtifact( artifact );
            }
            else
            {
                return model;
            }
        }
    }
