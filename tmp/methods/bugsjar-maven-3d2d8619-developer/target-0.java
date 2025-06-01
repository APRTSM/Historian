    private void getDownstreamProjects( String projectId, Set<String> projectIds, boolean transitive )
    {
        for ( String id : sorter.getDependents( projectId ) )
        {
            if ( projectIds.add( id ) && transitive )
            {
                getDownstreamProjects( id, projectIds, transitive );
            }
        }
    }
    public List<MavenProject> getUpstreamProjects( MavenProject project, boolean transitive )
    {
        if ( project == null )
        {
            throw new IllegalArgumentException( "project missing" );
        }

        Set<String> projectIds = new HashSet<String>();

        getUpstreamProjects( ProjectSorter.getId( project ), projectIds, transitive );

        return getSortedProjects( projectIds );
    }
    public List<MavenProject> getDownstreamProjects( MavenProject project, boolean transitive )
    {
        if ( project == null )
        {
            throw new IllegalArgumentException( "project missing" );
        }

        Set<String> projectIds = new HashSet<String>();

        getDownstreamProjects( ProjectSorter.getId( project ), projectIds, transitive );

        return getSortedProjects( projectIds );
    }
    private List<MavenProject> getSortedProjects( Set<String> projectIds )
    {
        List<MavenProject> result = new ArrayList<MavenProject>( projectIds.size() );

        for ( MavenProject mavenProject : sorter.getSortedProjects() )
        {
            if ( projectIds.contains( ProjectSorter.getId( mavenProject ) ) )
            {
                result.add( mavenProject );
            }
        }

        return result;
    }
    public DefaultProjectDependencyGraph( Collection<MavenProject> projects )
        throws CycleDetectedException, DuplicateProjectException
    {
        this.sorter = new ProjectSorter( projects );
    }
