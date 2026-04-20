    public void afterExecutionFailure( MojoExecutionEvent event )
    {
        for ( WeakMojoExecutionListener provided : getProvidedListeners() )
        {
            provided.afterExecutionFailure( event );
        }
    }
    public void afterMojoExecutionSuccess( MojoExecutionEvent event )
        throws MojoExecutionException
    {
        for ( WeakMojoExecutionListener provided : getProvidedListeners() )
        {
            provided.afterMojoExecutionSuccess( event );
        }
    }
    public void beforeMojoExecution( MojoExecutionEvent event )
        throws MojoExecutionException
    {
        for ( WeakMojoExecutionListener provided : getProvidedListeners() )
        {
            provided.beforeMojoExecution( event );
        }
    }
    private Collection<WeakMojoExecutionListener> getProvidedListeners()
    {
        // the same instance can be provided multiple times under different Key's
        // deduplicate instances to avoid redundant beforeXXX/afterXXX callbacks
        IdentityHashMap<WeakMojoExecutionListener, Object> listeners =
            new IdentityHashMap<WeakMojoExecutionListener, Object>();
        for ( Object provided : getScopeState().provided.values() )
        {
            if ( provided instanceof WeakMojoExecutionListener )
            {
                listeners.put( (WeakMojoExecutionListener) provided, null );
            }
        }
        return listeners.keySet();
    }
