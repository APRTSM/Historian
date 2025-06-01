    private boolean validatePluginVersion( String fieldName, ModelProblemCollector problems, String string,
                                           String sourceHint, InputLocationTracker tracker,
                                           ModelBuildingRequest request )
    {
        Severity errOn30 = getSeverity( request, ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_3_0 );

        if ( string == null )
        {
            // NOTE: The check for missing plugin versions is handled directly by the model builder
            return true;
        }

        if ( string.length() > 0 && !hasExpression( string ) && !"RELEASE".equals( string )
            && !"LATEST".equals( string ) )
        {
            return true;
        }

        addViolation( problems, errOn30, fieldName, sourceHint, "must be a valid version but is '" + string + "'.",
                      tracker );

        return false;
    }
    private boolean validateVersion( String fieldName, ModelProblemCollector problems, Severity severity,
                                     String string, String sourceHint, InputLocationTracker tracker )
    {
        if ( string == null || string.length() <= 0 )
        {
            return true;
        }

        if ( !hasExpression( string ) )
        {
            return true;
        }

        addViolation( problems, severity, fieldName, sourceHint, "must be a valid version but is '" + string + "'.",
                      tracker );

        return false;
    }
