    private boolean validateStringNotEmpty( SettingsProblemCollector problems, String fieldName, String string )
    {
        return validateStringNotEmpty( problems, fieldName, string, null );
    }
    private void validateRepositories( SettingsProblemCollector problems, List<Repository> repositories, String prefix )
    {
        for ( Repository repository : repositories )
        {
            validateStringNotEmpty( problems, prefix + ".id", repository.getId() );

            validateStringNotEmpty( problems, prefix + ".url", repository.getUrl() );
        }
    }
    public void validate( Settings settings, SettingsProblemCollector problems )
    {
        List<Profile> profiles = settings.getProfiles();

        if ( profiles != null )
        {
            for ( Profile prof : profiles )
            {
                validateRepositories( problems, prof.getRepositories(), "repositories.repository" );
                validateRepositories( problems, prof.getPluginRepositories(), "pluginRepositories.pluginRepository" );
            }
        }
    }
