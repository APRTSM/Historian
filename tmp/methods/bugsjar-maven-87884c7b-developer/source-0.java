    public List<String> getCompileClasspathElements()
        throws DependencyResolutionRequiredException
    {
        List<String> list = new ArrayList<String>( getArtifacts().size() + 1 );

        list.add( getBuild().getOutputDirectory() );

        for ( Artifact a : getArtifacts() )
        {                        
            if ( a.getArtifactHandler().isAddedToClasspath() )
            {
                // TODO: let the scope handler deal with this
                if ( Artifact.SCOPE_COMPILE.equals( a.getScope() ) || Artifact.SCOPE_PROVIDED.equals( a.getScope() ) || Artifact.SCOPE_SYSTEM.equals( a.getScope() ) )
                {
                    addArtifactPath( a, list );
                }
            }
        }

        return list;
    }
    public List<String> getSystemClasspathElements()
        throws DependencyResolutionRequiredException
    {
        List<String> list = new ArrayList<String>( getArtifacts().size() );

        list.add( getBuild().getOutputDirectory() );

        for ( Artifact a : getArtifacts() )
        {
            if ( a.getArtifactHandler().isAddedToClasspath() )
            {
                // TODO: let the scope handler deal with this
                if ( Artifact.SCOPE_SYSTEM.equals( a.getScope() ) )
                {
                    addArtifactPath( a, list );
                }
            }
        }
        return list;
    }
    public List<String> getRuntimeClasspathElements()
        throws DependencyResolutionRequiredException
    {
        List<String> list = new ArrayList<String>( getArtifacts().size() + 1 );

        list.add( getBuild().getOutputDirectory() );

        for ( Artifact a : getArtifacts() )
        {
            if ( a.getArtifactHandler().isAddedToClasspath() )
            {
                // TODO: let the scope handler deal with this
                if ( Artifact.SCOPE_COMPILE.equals( a.getScope() ) || Artifact.SCOPE_RUNTIME.equals( a.getScope() ) )
                {
                    addArtifactPath( a, list );
                }
            }
        }
        return list;
    }
    public List<String> getTestClasspathElements()
        throws DependencyResolutionRequiredException
    {
        List<String> list = new ArrayList<String>( getArtifacts().size() + 2 );

        list.add( getBuild().getTestOutputDirectory() );

        list.add( getBuild().getOutputDirectory() );
        
        for ( Artifact a : getArtifacts() )
        {            
            if ( a.getArtifactHandler().isAddedToClasspath() )
            {                
                addArtifactPath( a, list );
            }
        }

        return list;
    }
