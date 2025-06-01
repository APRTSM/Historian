    public MavenProject getParent()
    {
        if ( parent == null )
        {
            /*
             * TODO: This is suboptimal. Without a cache in the project builder, rebuilding the parent chain currently
             * causes O(n^2) parser invocations for an inheritance hierarchy of depth n.
             */
            if ( parentFile != null )
            {
                checkProjectBuildingRequest();
                ProjectBuildingRequest request = new DefaultProjectBuildingRequest( projectBuilderConfiguration );
                request.setRemoteRepositories( getRemoteArtifactRepositories() );

                try
                {
                    parent = mavenProjectBuilder.build( parentFile, request ).getProject();
                }
                catch ( ProjectBuildingException e )
                {
                    if ( logger != null )
                    {
                        logger.error( "Failed to build parent project for " + getId(), e );
                    }
                    parent = ERROR_BUILDING_PARENT;
                }
            }
            else if ( model.getParent() != null )
            {
                checkProjectBuildingRequest();
                ProjectBuildingRequest request = new DefaultProjectBuildingRequest( projectBuilderConfiguration );
                request.setRemoteRepositories( getRemoteArtifactRepositories() );

                try
                {
                    parent = mavenProjectBuilder.build( getParentArtifact(), request ).getProject();
                }
                catch ( ProjectBuildingException e )
                {
                    if ( logger != null )
                    {
                        logger.error( "Failed to build parent project for " + getId(), e );
                    }
                    parent = ERROR_BUILDING_PARENT;
                }
            }
        }
        return parent == ERROR_BUILDING_PARENT ? null : parent;
    }
