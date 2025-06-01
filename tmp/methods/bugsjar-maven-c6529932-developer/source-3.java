    private boolean isActive( Profile profile, ProfileActivationContext context, ModelProblemCollector problems )
    {
        for ( ProfileActivator activator : activators )
        {
            try
            {
                if ( activator.isActive( profile, context, problems ) )
                {
                    return true;
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
        return false;
    }
