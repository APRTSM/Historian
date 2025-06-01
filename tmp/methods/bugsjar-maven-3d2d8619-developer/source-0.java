    public List<MavenProject> getDownstreamProjects( MavenProject project, boolean transitive )
    {
        if ( project == null )
        {
            throw new IllegalArgumentException( "project missing" );
        }

        Collection<String> projectIds = new HashSet<String>();

        getDownstreamProjects( ProjectSorter.getId( project ), projectIds, transitive );

        return getProjects( projectIds );
    }
    public List<MavenProject> getUpstreamProjects( MavenProject project, boolean transitive )
    {
        if ( project == null )
        {
            throw new IllegalArgumentException( "project missing" );
        }

        Collection<String> projectIds = new HashSet<String>();

        getUpstreamProjects( ProjectSorter.getId( project ), projectIds, transitive );

        return getProjects( projectIds );
    }
    private void getDownstreamProjects( String projectId, Collection<String> projectIds, boolean transitive )
    {
        for ( String id : sorter.getDependents( projectId ) )
        {
            if ( projectIds.add( id ) && transitive )
            {
                getDownstreamProjects( id, projectIds, transitive );
            }
        }
    }
    private List<MavenProject> getProjects( Collection<String> projectIds )
    {
        List<MavenProject> projects = new ArrayList<MavenProject>( projectIds.size() );

        for ( String projectId : projectIds )
        {
            MavenProject project = sorter.getProjectMap().get( projectId );

            if ( project != null )
            {
                projects.add( project );
            }
        }

        return projects;
    }
    public DefaultProjectDependencyGraph( Collection<MavenProject> projects ) throws CycleDetectedException, DuplicateProjectException
    {
        this.sorter = new ProjectSorter( projects );
    }
