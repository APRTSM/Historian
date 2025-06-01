    private MavenExecutionResult doExecute( MavenExecutionRequest request )
    {
        if ( request.getStartTime() != null )
        {
            request.getSystemProperties().put( "${build.timestamp}",
                                               new SimpleDateFormat( "yyyyMMdd-hhmm" ).format( request.getStartTime() ) );
        }

        request.setStartTime( new Date() );

        MavenExecutionResult result = new DefaultMavenExecutionResult();

        try
        {
            validateLocalRepository( request );
        }
        catch ( LocalRepositoryNotAccessibleException e )
        {
            return addExceptionToResult( result, e );
        }

        DefaultRepositorySystemSession repoSession = (DefaultRepositorySystemSession) newRepositorySession( request );

        MavenSession session = new MavenSession( container, repoSession, request, result );
        legacySupport.setSession( session );

        try
        {
            for ( AbstractMavenLifecycleParticipant listener : getLifecycleParticipants( Collections.<MavenProject> emptyList() ) )
            {
                listener.afterSessionStart( session );
            }
        }
        catch ( MavenExecutionException e )
        {
            return addExceptionToResult( result, e );
        }

        eventCatapult.fire( ExecutionEvent.Type.ProjectDiscoveryStarted, session, null );

        List<MavenProject> projects;
        try
        {
            projects = getProjectsForMavenReactor( session );
            //
            // Capture the full set of projects before any potential constraining is performed by --projects
            //
            session.setAllProjects( projects );
        }
        catch ( ProjectBuildingException e )
        {
            return addExceptionToResult( result, e );
        }

        validateProjects( projects );

        //
        // This creates the graph and trims the projects down based on the user request using something like:
        //
        // -pl project0,project2 eclipse:eclipse
        //
        ProjectDependencyGraph projectDependencyGraph = createProjectDependencyGraph( projects, request, result, true );

        session.setProjects( projectDependencyGraph.getSortedProjects() );
        
        if ( result.hasExceptions() )
        {
            return result;
        }

        try
        {
            session.setProjectMap( getProjectMap( session.getProjects() ) );
        }
        catch ( DuplicateProjectException e )
        {
            return addExceptionToResult( result, e );
        }
        
        WorkspaceReader reactorWorkspace;
        sessionScope.enter();
        sessionScope.seed( MavenSession.class, session );
        try
        {
            reactorWorkspace = container.lookup( WorkspaceReader.class, ReactorReader.HINT );
        }
        catch ( ComponentLookupException e )
        {
            return addExceptionToResult( result, e );
        }
        
        //
        // Desired order of precedence for local artifact repositories
        //
        // Reactor
        // Workspace
        // User Local Repository
        //        
        repoSession.setWorkspaceReader( ChainedWorkspaceReader.newInstance( reactorWorkspace,
                                                                            repoSession.getWorkspaceReader() ) );

        repoSession.setReadOnly();

        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try
        {
            for ( AbstractMavenLifecycleParticipant listener : getLifecycleParticipants( projects ) )
            {
                Thread.currentThread().setContextClassLoader( listener.getClass().getClassLoader() );

                listener.afterProjectsRead( session );
            }
        }
        catch ( MavenExecutionException e )
        {
            return addExceptionToResult( result, e );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( originalClassLoader );
        }

        //
        // The projects need to be topologically after the participants have run their afterProjectsRead(session)
        // because the participant is free to change the dependencies of a project which can potentially change the
        // topological order of the projects, and therefore can potentially change the build order.
        //
        // Note that participants may affect the topological order of the projects but it is
        // not expected that a participant will add or remove projects from the session.
        //
        projectDependencyGraph = createProjectDependencyGraph( session.getProjects(), request, result, false );

        if ( result.hasExceptions() )
        {
            try 
            {
                afterSessionEnd( projects, session );
            } 
            catch ( MavenExecutionException e )
            {
                return addExceptionToResult( result, e );
            }

            return result;
        }
        
        session.setProjects( projectDependencyGraph.getSortedProjects() );

        session.setProjectDependencyGraph( projectDependencyGraph );

        result.setTopologicallySortedProjects( session.getProjects() );

        result.setProject( session.getTopLevelProject() );

        lifecycleStarter.execute( session );

        validateActivatedProfiles( session.getProjects(), request.getActiveProfiles() );

        if ( session.getResult().hasExceptions() )
        {
            return addExceptionToResult( result, session.getResult().getExceptions().get( 0 ) );
        }

        try 
        {
            afterSessionEnd( projects, session );
        } 
        catch ( MavenExecutionException e )
        {
            return addExceptionToResult( result, e );
        }

        sessionScope.exit();

        return result;
    }
