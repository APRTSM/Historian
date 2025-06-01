    private boolean validatePluginVersion( String fieldName, ModelProblemCollector problems, String string,
                                           String sourceHint, InputLocationTracker tracker,
                                           ModelBuildingRequest request )
    {
        if ( string == null )
        {
            // NOTE: The check for missing plugin versions is handled directly by the model builder
            return true;
        }

        Severity errOn30 = getSeverity( request, ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_3_0 );

        if ( !validateVersion( fieldName, problems, errOn30, string, sourceHint, tracker ) )
        {
            return false;
        }

        if ( string.length() <= 0 || "RELEASE".equals( string ) || "LATEST".equals( string ) )
        {
            addViolation( problems, errOn30, fieldName, sourceHint, "must be a valid version but is '" + string + "'.",
                          tracker );
            return false;
        }

        return true;
    }
    private boolean validateVersion( String fieldName, ModelProblemCollector problems, Severity severity,
                                     String string, String sourceHint, InputLocationTracker tracker )
    {
        if ( string == null || string.length() <= 0 )
        {
            return true;
        }

        if ( hasExpression( string ) )
        {
            addViolation( problems, severity, fieldName, sourceHint,
                          "must be a valid version but is '" + string + "'.", tracker );
            return false;
        }

        if ( !validateBannedCharacters( fieldName, problems, severity, string, sourceHint, tracker,
                                        ILLEGAL_VERSION_CHARS ) )
        {
            return false;
        }

        return true;
    }
