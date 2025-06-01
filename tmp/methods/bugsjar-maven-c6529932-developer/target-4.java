    private boolean isActive( Profile profile, ProfileActivationContext context, ModelProblemCollector problems )
    {
        boolean isActive = false;
        for ( ProfileActivator activator : activators ) {
            if ( activator.presentInConfig( profile, context, problems ) ) {
                isActive = true;
            }
        }
        for ( ProfileActivator activator : activators )
        {
            try
            {
                if ( activator.presentInConfig( profile, context, problems ) )
                {
                    isActive &=  activator.isActive( profile, context, problems );
                }
            }
            catch ( RuntimeException e )
            {
                problems.add( new ModelProblemCollectorRequest( Severity.ERROR, Version.BASE )
                        .setMessage( "Failed to determine activation for profile " + profile.getId() )
                        .setLocation( profile.getLocation( "" ) )
                        .setException( e ) );
                return false;
            }
        }
        return isActive;
    }
    public boolean presentInConfig( Profile profile, ProfileActivationContext context, ModelProblemCollector problems )
    {
        Activation activation = profile.getActivation();

        if ( activation == null )
        {
            return false;
        }

        ActivationFile file = activation.getFile();

        if ( file == null )
        {
            return false;
        }
        return true;
    }
    public boolean presentInConfig( Profile profile, ProfileActivationContext context, ModelProblemCollector problems )
    {
        Activation activation = profile.getActivation();

        if ( activation == null )
        {
            return false;
        }

        String jdk = activation.getJdk();

        if ( jdk == null )
        {
            return false;
        }
        return true;
    }
    public boolean presentInConfig( Profile profile, ProfileActivationContext context, ModelProblemCollector problems )
    {
        Activation activation = profile.getActivation();

        if ( activation == null )
        {
            return false;
        }

        ActivationOS os = activation.getOs();

        if ( os == null )
        {
            return false;
        }
        return true;
    }
    boolean isActive( Profile profile, ProfileActivationContext context, ModelProblemCollector problems );

    /**
     * Determines whether specified activation method is present in configuration or not. It should help to have AND between
     * activation conditions
     * Need for solving http://jira.codehaus.org/browse/MNG-4565
     * @param profile The profile whose activation status should be determined, must not be {@code null}.
    boolean presentInConfig( Profile profile, ProfileActivationContext context, ModelProblemCollector problems );
