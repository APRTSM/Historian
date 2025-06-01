    public boolean isActive( Profile profile )
    {
        Activation activation = profile.getActivation();
        ActivationOS os = activation.getOs();

        boolean result = ensureAtLeastOneNonNull( os );

        if ( result && os.getFamily() != null )
        {
            result = determineFamilyMatch( os.getFamily() );
        }
        if ( result && os.getName() != null )
        {
            result = determineNameMatch( os.getName() );
        }
        if ( result && os.getArch() != null )
        {
            result = determineArchMatch( os.getArch() );
        }
        if ( result && os.getVersion() != null )
        {
            result = determineVersionMatch( os.getVersion() );
        }
        return result;
    }
    private boolean determineNameMatch( String name )
    {
        String test = name;
        boolean reverse = false;
        
        if ( test.startsWith( "!" ) )
        {
            reverse = true;
            test = test.substring( 1 );
        }

        boolean result = Os.isName( test );

        if ( reverse )
        {
            return !result;
        }
        else
        {
            return result;
        }
    }
