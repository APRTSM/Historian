    public void validate( Settings settings, SettingsProblemCollector problems )
    {
        if ( settings.isUsePluginRegistry() )
        {
            addWarn( problems, "'usePluginRegistry' is deprecated and has no effect." );
        }

        List<String> pluginGroups = settings.getPluginGroups();

        if ( pluginGroups != null )
        {
            for ( int i = 0; i < pluginGroups.size(); i++ )
            {
                String pluginGroup = pluginGroups.get( i ).trim();

                if ( StringUtils.isBlank( pluginGroup ) )
                {
                    addError( problems, "'pluginGroups.pluginGroup[" + i + "]' must not be empty." );
                }
                else if ( !pluginGroup.matches( ID_REGEX ) )
                {
                    addError( problems, "'pluginGroups.pluginGroup[" + i
                        + "]' must denote a valid group id and match the pattern " + ID_REGEX );
                }
            }
        }

        List<Server> servers = settings.getServers();

        if ( servers != null )
        {
            for ( int i = 0; i < servers.size(); i++ )
            {
                Server server = servers.get( i );

                validateStringNotEmpty( problems, "servers.server[" + i + "].id", server.getId(), null );
            }
        }

        List<Mirror> mirrors = settings.getMirrors();

        if ( mirrors != null )
        {
            for ( Mirror mirror : mirrors )
            {
                validateStringNotEmpty( problems, "mirrors.mirror.id", mirror.getId(), mirror.getUrl() );

                validateStringNotEmpty( problems, "mirrors.mirror.url", mirror.getUrl(), mirror.getId() );

                validateStringNotEmpty( problems, "mirrors.mirror.mirrorOf", mirror.getMirrorOf(), mirror.getId() );
            }
        }

        List<Profile> profiles = settings.getProfiles();

        if ( profiles != null )
        {
            for ( Profile profile : profiles )
            {
                validateRepositories( problems, profile.getRepositories(), "repositories.repository" );
                validateRepositories( problems, profile.getPluginRepositories(), "pluginRepositories.pluginRepository" );
            }
        }
    }
    private void addWarn( SettingsProblemCollector problems, String msg )
    {
        problems.add( SettingsProblem.Severity.WARNING, msg, -1, -1, null );
    }
    private void validateRepositories( SettingsProblemCollector problems, List<Repository> repositories, String prefix )
    {
        for ( Repository repository : repositories )
        {
            validateStringNotEmpty( problems, prefix + ".id", repository.getId(), repository.getUrl() );

            validateStringNotEmpty( problems, prefix + ".url", repository.getUrl(), repository.getId() );

            if ( "legacy".equals( repository.getLayout() ) )
            {
                addWarn( problems, "'" + prefix + ".layout' for " + repository.getId()
                    + " uses the deprecated value 'legacy'." );
            }
        }
    }
