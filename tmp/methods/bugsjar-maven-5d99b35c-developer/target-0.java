    public DefaultProfileManager( PlexusContainer container, Properties props )
    {
        this( container, (Settings)null, props );
        
    }
    public DefaultProfileManager( PlexusContainer container, Settings settings, Properties props )
    {
        this.container = container;

        loadSettingsProfiles( settings );

        if ( props != null )
        {
            systemProperties = props;
        }
    }
