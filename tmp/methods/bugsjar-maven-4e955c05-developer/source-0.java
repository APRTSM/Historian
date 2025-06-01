    public boolean isActive( Profile profile )
    {
        Activation activation = profile.getActivation();
        ActivationOS os = activation.getOs();
        
        boolean hasNonNull = ensureAtLeastOneNonNull( os );
        
        boolean isFamily = determineFamilyMatch( os.getFamily() );
        boolean isName = determineNameMatch( os.getName() );
        boolean isArch = determineArchMatch( os.getArch() );
        boolean isVersion = determineVersionMatch( os.getVersion() );
        
        return hasNonNull && isFamily && isName && isArch && isVersion;
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
