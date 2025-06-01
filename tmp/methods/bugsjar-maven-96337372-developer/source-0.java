    public void afterMojoExecutionSuccess( MojoExecutionEvent event )
        throws MojoExecutionException
    {
        for ( Object provided : getScopeState().provided.values() )
        {
            if ( provided instanceof WeakMojoExecutionListener )
            {
                ( (WeakMojoExecutionListener) provided ).afterMojoExecutionSuccess( event );
            }
        }
    }
    public void afterExecutionFailure( MojoExecutionEvent event )
    {
        for ( Object provided : getScopeState().provided.values() )
        {
            if ( provided instanceof WeakMojoExecutionListener )
            {
                ( (WeakMojoExecutionListener) provided ).afterExecutionFailure( event );
            }
        }
    }
    public void beforeMojoExecution( MojoExecutionEvent event )
        throws MojoExecutionException
    {
        for ( Object provided : getScopeState().provided.values() )
        {
            if ( provided instanceof WeakMojoExecutionListener )
            {
                ( (WeakMojoExecutionListener) provided ).beforeMojoExecution( event );
            }
        }
    }
